package com.acme.transport.fleetops.inspection.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the inspection target calculation. */
interface InspectionTargetModifier {
    fun describe(): String
    fun adjust(item: InspectionTarget, value: Double): Double
}

data class RoundingInspectionTargetAdjuster(var step: Double) : InspectionTargetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionTarget, value: Double): Double = roundTo(value, step)
}

data class FlaggedInspectionTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionTargetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentInspectionTargetAdjuster(var percent: Double) : InspectionTargetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: InspectionTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumInspectionTargetAdjuster(var minimum: Double) : InspectionTargetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class InspectionTargetOdometerOffsetAdjuster(var weight: Double) : InspectionTargetModifier {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: InspectionTarget, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class CapInspectionTargetAdjuster(var cap: Double) : InspectionTargetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionTarget, value: Double): Double = minOf(value, cap)
}

/** Runs the inspection target steps in order and keeps a trace of each. */
data class InspectionTargetEngine(
    var steps: MutableList<InspectionTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: InspectionTarget): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionTargetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultInspectionTargetEngine(flags: Registry): InspectionTargetEngine {
    return InspectionTargetEngine(
        steps = mutableListOf<InspectionTargetModifier>(
            RoundingInspectionTargetAdjuster(0.5),
            FlaggedInspectionTargetAdjuster(flags, "inspection-target-legacy", -1.7),
            PercentInspectionTargetAdjuster(24.1),
            MinimumInspectionTargetAdjuster(34.0),
            InspectionTargetOdometerOffsetAdjuster(0.97),
            CapInspectionTargetAdjuster(20305.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 96927.6,
    )
}
