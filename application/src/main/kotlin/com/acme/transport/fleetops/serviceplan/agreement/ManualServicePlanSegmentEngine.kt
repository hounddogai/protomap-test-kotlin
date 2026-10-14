package com.acme.transport.fleetops.serviceplan.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual service plan segment calculation. */
interface ManualServicePlanSegmentAdjuster {
    fun describe(): String
    fun adjust(item: ManualServicePlanSegment, value: Double): Double
}

data class ManualServicePlanSegmentCostOffsetAdjuster(var weight: Double) : ManualServicePlanSegmentAdjuster {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: ManualServicePlanSegment, value: Double): Double = value + item.cost * weight
}

data class FlaggedManualServicePlanSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualServicePlanSegmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualServicePlanSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingManualServicePlanSegmentAdjuster(var step: Double) : ManualServicePlanSegmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualServicePlanSegment, value: Double): Double = roundTo(value, step)
}

data class MinimumManualServicePlanSegmentAdjuster(var minimum: Double) : ManualServicePlanSegmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualServicePlanSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapManualServicePlanSegmentAdjuster(var cap: Double) : ManualServicePlanSegmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualServicePlanSegment, value: Double): Double = minOf(value, cap)
}

data class PercentManualServicePlanSegmentAdjuster(var percent: Double) : ManualServicePlanSegmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualServicePlanSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the manual service plan segment steps in order and keeps a trace of each. */
data class ManualServicePlanSegmentEngine(
    var steps: MutableList<ManualServicePlanSegmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualServicePlanSegment): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualServicePlanSegmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ManualServicePlanSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualServicePlanSegmentEngine(flags: Registry): ManualServicePlanSegmentEngine {
    return ManualServicePlanSegmentEngine(
        steps = mutableListOf<ManualServicePlanSegmentAdjuster>(
            ManualServicePlanSegmentCostOffsetAdjuster(1.01),
            FlaggedManualServicePlanSegmentAdjuster(flags, "manual-service-plan-segment-scheduled", -5.1),
            RoundingManualServicePlanSegmentAdjuster(0.1),
            MinimumManualServicePlanSegmentAdjuster(17.0),
            CapManualServicePlanSegmentAdjuster(46551.0),
            PercentManualServicePlanSegmentAdjuster(-0.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 94853.2,
    )
}
