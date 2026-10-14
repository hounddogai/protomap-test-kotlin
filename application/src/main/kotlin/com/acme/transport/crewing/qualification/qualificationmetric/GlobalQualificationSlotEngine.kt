package com.acme.transport.crewing.qualification.qualificationmetric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global qualification slot calculation. */
interface GlobalQualificationSlotModifier {
    fun describe(): String
    fun adjust(item: GlobalQualificationSlot, value: Double): Double
}

data class MinimumGlobalQualificationSlotAdjuster(var minimum: Double) : GlobalQualificationSlotModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalQualificationSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedGlobalQualificationSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalQualificationSlotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalQualificationSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentGlobalQualificationSlotAdjuster(var percent: Double) : GlobalQualificationSlotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalQualificationSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingGlobalQualificationSlotAdjuster(var step: Double) : GlobalQualificationSlotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalQualificationSlot, value: Double): Double = roundTo(value, step)
}

data class TieredGlobalQualificationSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalQualificationSlotModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalQualificationSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapGlobalQualificationSlotAdjuster(var cap: Double) : GlobalQualificationSlotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalQualificationSlot, value: Double): Double = minOf(value, cap)
}

/** Runs the global qualification slot steps in order and keeps a trace of each. */
data class GlobalQualificationSlotEngine(
    var steps: MutableList<GlobalQualificationSlotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalQualificationSlot): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalQualificationSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalQualificationSlotEngine(flags: Registry): GlobalQualificationSlotEngine {
    return GlobalQualificationSlotEngine(
        steps = mutableListOf<GlobalQualificationSlotModifier>(
            MinimumGlobalQualificationSlotAdjuster(37.6),
            FlaggedGlobalQualificationSlotAdjuster(flags, "global-qualification-slot-strict", 1.7),
            PercentGlobalQualificationSlotAdjuster(-3.2),
            RoundingGlobalQualificationSlotAdjuster(0.1),
            TieredGlobalQualificationSlotAdjuster(
                mutableListOf<Double>(838.0, 5078.0, 5710.0),
                mutableListOf<Double>(1.1, 0.85, 0.97),
            ),
            CapGlobalQualificationSlotAdjuster(24670.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 48996.2,
    )
}
