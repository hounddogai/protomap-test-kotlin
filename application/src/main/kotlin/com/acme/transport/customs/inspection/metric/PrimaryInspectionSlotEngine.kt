package com.acme.transport.customs.inspection.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the primary inspection slot calculation. */
interface PrimaryInspectionSlotStep {
    fun describe(): String
    fun adjust(item: PrimaryInspectionSlot, value: Double): Double
}

data class CapPrimaryInspectionSlotAdjuster(var cap: Double) : PrimaryInspectionSlotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryInspectionSlot, value: Double): Double = minOf(value, cap)
}

data class FlaggedPrimaryInspectionSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryInspectionSlotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryInspectionSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingPrimaryInspectionSlotAdjuster(var step: Double) : PrimaryInspectionSlotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryInspectionSlot, value: Double): Double = roundTo(value, step)
}

data class MinimumPrimaryInspectionSlotAdjuster(var minimum: Double) : PrimaryInspectionSlotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryInspectionSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredPrimaryInspectionSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryInspectionSlotStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryInspectionSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentPrimaryInspectionSlotAdjuster(var percent: Double) : PrimaryInspectionSlotStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryInspectionSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the primary inspection slot steps in order and keeps a trace of each. */
data class PrimaryInspectionSlotEngine(
    var steps: MutableList<PrimaryInspectionSlotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryInspectionSlot): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryInspectionSlotStep = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryInspectionSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryInspectionSlotEngine(flags: Registry): PrimaryInspectionSlotEngine {
    return PrimaryInspectionSlotEngine(
        steps = mutableListOf<PrimaryInspectionSlotStep>(
            CapPrimaryInspectionSlotAdjuster(21200.0),
            FlaggedPrimaryInspectionSlotAdjuster(flags, "primary-inspection-slot-default", -2.3),
            RoundingPrimaryInspectionSlotAdjuster(0.1),
            MinimumPrimaryInspectionSlotAdjuster(50.0),
            TieredPrimaryInspectionSlotAdjuster(
                mutableListOf<Double>(2984.0, 4455.0, 5748.0),
                mutableListOf<Double>(1.06, 1.14, 1.09),
            ),
            PercentPrimaryInspectionSlotAdjuster(-6.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 78059.4,
    )
}
