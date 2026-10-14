package com.acme.transport.emissions.evcharge.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the ev charge milestone calculation. */
interface EvChargeMilestoneFactor {
    fun describe(): String
    fun adjust(item: EvChargeMilestone, value: Double): Double
}

data class CapEvChargeMilestoneAdjuster(var cap: Double) : EvChargeMilestoneFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EvChargeMilestone, value: Double): Double = minOf(value, cap)
}

data class MinimumEvChargeMilestoneAdjuster(var minimum: Double) : EvChargeMilestoneFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EvChargeMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class EvChargeMilestoneFuelLitersOffsetAdjuster(var weight: Double) : EvChargeMilestoneFactor {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: EvChargeMilestone, value: Double): Double = value + item.fuelLiters * weight
}

data class FlaggedEvChargeMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EvChargeMilestoneFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EvChargeMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the ev charge milestone steps in order and keeps a trace of each. */
data class EvChargeMilestoneEngine(
    var steps: MutableList<EvChargeMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: EvChargeMilestone): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<EvChargeMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultEvChargeMilestoneEngine(flags: Registry): EvChargeMilestoneEngine {
    return EvChargeMilestoneEngine(
        steps = mutableListOf<EvChargeMilestoneFactor>(
            CapEvChargeMilestoneAdjuster(21237.0),
            MinimumEvChargeMilestoneAdjuster(11.3),
            EvChargeMilestoneFuelLitersOffsetAdjuster(1.38),
            FlaggedEvChargeMilestoneAdjuster(flags, "ev-charge-milestone-legacy", 1.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 74380.3,
    )
}
