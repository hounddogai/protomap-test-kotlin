package com.acme.transport.fleetops.inspection.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient inspection window calculation. */
interface LenientInspectionWindowModifier {
    fun describe(): String
    fun adjust(item: LenientInspectionWindow, value: Double): Double
}

data class RoundingLenientInspectionWindowAdjuster(var step: Double) : LenientInspectionWindowModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientInspectionWindow, value: Double): Double = roundTo(value, step)
}

data class MinimumLenientInspectionWindowAdjuster(var minimum: Double) : LenientInspectionWindowModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientInspectionWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLenientInspectionWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientInspectionWindowModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientInspectionWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LenientInspectionWindowUpdatedAtOffsetAdjuster(
    var weight: Double,
) : LenientInspectionWindowModifier {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: LenientInspectionWindow, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

data class PercentLenientInspectionWindowAdjuster(var percent: Double) : LenientInspectionWindowModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientInspectionWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredLenientInspectionWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientInspectionWindowModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientInspectionWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lenient inspection window steps in order and keeps a trace of each. */
data class LenientInspectionWindowEngine(
    var steps: MutableList<LenientInspectionWindowModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientInspectionWindow): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientInspectionWindowModifier = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientInspectionWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientInspectionWindowEngine(flags: Registry): LenientInspectionWindowEngine {
    return LenientInspectionWindowEngine(
        steps = mutableListOf<LenientInspectionWindowModifier>(
            RoundingLenientInspectionWindowAdjuster(1.0),
            MinimumLenientInspectionWindowAdjuster(42.1),
            FlaggedLenientInspectionWindowAdjuster(flags, "lenient-inspection-window-regional", -2.4),
            LenientInspectionWindowUpdatedAtOffsetAdjuster(0.02),
            PercentLenientInspectionWindowAdjuster(3.3),
            TieredLenientInspectionWindowAdjuster(
                mutableListOf<Double>(171.0, 1135.0, 4049.0, 5716.0),
                mutableListOf<Double>(0.83, 0.98, 0.96, 1.15),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 62757.6,
    )
}
