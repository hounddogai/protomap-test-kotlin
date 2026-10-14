package com.acme.transport.fleetops.inspection.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the inspection threshold calculation. */
interface InspectionThresholdFactor {
    fun describe(): String
    fun adjust(item: InspectionThreshold, value: Double): Double
}

data class SourceInspectionThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : InspectionThresholdFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: InspectionThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingInspectionThresholdAdjuster(var step: Double) : InspectionThresholdFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionThreshold, value: Double): Double = roundTo(value, step)
}

data class FlaggedInspectionThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionThresholdFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the inspection threshold steps in order and keeps a trace of each. */
data class InspectionThresholdEngine(
    var steps: MutableList<InspectionThresholdFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: InspectionThreshold): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionThresholdFactor = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultInspectionThresholdEngine(flags: Registry): InspectionThresholdEngine {
    return InspectionThresholdEngine(
        steps = mutableListOf<InspectionThresholdFactor>(
            SourceInspectionThresholdAdjuster(mutableMapOf<String, Double>("south" to 0.98)),
            RoundingInspectionThresholdAdjuster(0.05),
            FlaggedInspectionThresholdAdjuster(flags, "inspection-threshold-adaptive", 7.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 14150.6,
    )
}
