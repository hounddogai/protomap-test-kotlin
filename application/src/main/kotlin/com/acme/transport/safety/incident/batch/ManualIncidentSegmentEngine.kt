package com.acme.transport.safety.incident.batch

import com.acme.transport.kit.flags.Registry

/** One step of the manual incident segment calculation. */
interface ManualIncidentSegmentFactor {
    fun describe(): String
    fun adjust(item: ManualIncidentSegment, value: Double): Double
}

data class FlaggedManualIncidentSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualIncidentSegmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualIncidentSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ManualIncidentSegmentBudgetOffsetAdjuster(var weight: Double) : ManualIncidentSegmentFactor {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: ManualIncidentSegment, value: Double): Double = value + item.budget * weight
}

data class PercentManualIncidentSegmentAdjuster(var percent: Double) : ManualIncidentSegmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualIncidentSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SegmentManualIncidentSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualIncidentSegmentFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: ManualIncidentSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumManualIncidentSegmentAdjuster(var minimum: Double) : ManualIncidentSegmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualIncidentSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual incident segment steps in order and keeps a trace of each. */
data class ManualIncidentSegmentEngine(
    var steps: MutableList<ManualIncidentSegmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualIncidentSegment): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualIncidentSegmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ManualIncidentSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualIncidentSegmentEngine(flags: Registry): ManualIncidentSegmentEngine {
    return ManualIncidentSegmentEngine(
        steps = mutableListOf<ManualIncidentSegmentFactor>(
            FlaggedManualIncidentSegmentAdjuster(flags, "manual-incident-segment-default", -2.3),
            ManualIncidentSegmentBudgetOffsetAdjuster(1.86),
            PercentManualIncidentSegmentAdjuster(17.0),
            SegmentManualIncidentSegmentAdjuster(mutableMapOf<String, Double>("east" to 0.8)),
            MinimumManualIncidentSegmentAdjuster(31.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 4094.4,
    )
}
