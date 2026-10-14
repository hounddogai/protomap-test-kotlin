package com.acme.transport.customs.inspection.inspectiontarget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional inspection target calculation. */
interface RegionalInspectionTargetStep {
    fun describe(): String
    fun adjust(item: RegionalInspectionTarget, value: Double): Double
}

data class RoundingRegionalInspectionTargetAdjuster(var step: Double) : RegionalInspectionTargetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalInspectionTarget, value: Double): Double = roundTo(value, step)
}

data class TieredRegionalInspectionTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalInspectionTargetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalInspectionTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRegionalInspectionTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalInspectionTargetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalInspectionTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionalInspectionTargetDurationMinutesOffsetAdjuster(
    var weight: Double,
) : RegionalInspectionTargetStep {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: RegionalInspectionTarget, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class MinimumRegionalInspectionTargetAdjuster(var minimum: Double) : RegionalInspectionTargetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalInspectionTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapRegionalInspectionTargetAdjuster(var cap: Double) : RegionalInspectionTargetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalInspectionTarget, value: Double): Double = minOf(value, cap)
}

/** Runs the regional inspection target steps in order and keeps a trace of each. */
data class RegionalInspectionTargetEngine(
    var steps: MutableList<RegionalInspectionTargetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalInspectionTarget): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalInspectionTargetStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalInspectionTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalInspectionTargetEngine(flags: Registry): RegionalInspectionTargetEngine {
    return RegionalInspectionTargetEngine(
        steps = mutableListOf<RegionalInspectionTargetStep>(
            RoundingRegionalInspectionTargetAdjuster(5.0),
            TieredRegionalInspectionTargetAdjuster(
                mutableListOf<Double>(740.0, 3144.0, 5699.0, 7464.0),
                mutableListOf<Double>(0.93, 1.06, 1.11, 1.0),
            ),
            FlaggedRegionalInspectionTargetAdjuster(flags, "regional-inspection-target-strict", 1.1),
            RegionalInspectionTargetDurationMinutesOffsetAdjuster(1.89),
            MinimumRegionalInspectionTargetAdjuster(4.4),
            CapRegionalInspectionTargetAdjuster(21817.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 73604.2,
    )
}
