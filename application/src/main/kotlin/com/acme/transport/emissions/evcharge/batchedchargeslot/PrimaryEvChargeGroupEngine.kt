package com.acme.transport.emissions.evcharge.batchedchargeslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary ev charge group calculation. */
interface PrimaryEvChargeGroupFactor {
    fun describe(): String
    fun adjust(item: PrimaryEvChargeGroup, value: Double): Double
}

data class PercentPrimaryEvChargeGroupAdjuster(var percent: Double) : PrimaryEvChargeGroupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryEvChargeGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumPrimaryEvChargeGroupAdjuster(var minimum: Double) : PrimaryEvChargeGroupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryEvChargeGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapPrimaryEvChargeGroupAdjuster(var cap: Double) : PrimaryEvChargeGroupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryEvChargeGroup, value: Double): Double = minOf(value, cap)
}

data class FlaggedPrimaryEvChargeGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryEvChargeGroupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryEvChargeGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the primary ev charge group steps in order and keeps a trace of each. */
data class PrimaryEvChargeGroupEngine(
    var steps: MutableList<PrimaryEvChargeGroupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryEvChargeGroup): Double {
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

    fun evaluateAll(items: MutableList<PrimaryEvChargeGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryEvChargeGroupEngine(flags: Registry): PrimaryEvChargeGroupEngine {
    return PrimaryEvChargeGroupEngine(
        steps = mutableListOf<PrimaryEvChargeGroupFactor>(
            PercentPrimaryEvChargeGroupAdjuster(1.8),
            MinimumPrimaryEvChargeGroupAdjuster(0.1),
            CapPrimaryEvChargeGroupAdjuster(13976.0),
            FlaggedPrimaryEvChargeGroupAdjuster(flags, "primary-ev-charge-group-capped", -4.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 50422.8,
    )
}
