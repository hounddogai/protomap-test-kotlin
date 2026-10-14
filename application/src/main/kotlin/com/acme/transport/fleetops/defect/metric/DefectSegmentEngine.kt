package com.acme.transport.fleetops.defect.metric

import com.acme.transport.kit.flags.Registry

/** One step of the defect segment calculation. */
interface DefectSegmentStep {
    fun describe(): String
    fun adjust(item: DefectSegment, value: Double): Double
}

data class FlaggedDefectSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefectSegmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefectSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelDefectSegmentAdjuster(var factors: MutableMap<String, Double>) : DefectSegmentStep {
    override fun describe(): String = "by label"

    override fun adjust(item: DefectSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentDefectSegmentAdjuster(var percent: Double) : DefectSegmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefectSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDefectSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefectSegmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefectSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the defect segment steps in order and keeps a trace of each. */
data class DefectSegmentEngine(
    var steps: MutableList<DefectSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefectSegment): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefectSegmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<DefectSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefectSegmentEngine(flags: Registry): DefectSegmentEngine {
    return DefectSegmentEngine(
        steps = mutableListOf<DefectSegmentStep>(
            FlaggedDefectSegmentAdjuster(flags, "defect-segment-seasonal", -6.2),
            LabelDefectSegmentAdjuster(mutableMapOf<String, Double>("north" to 0.91)),
            PercentDefectSegmentAdjuster(-13.5),
            TieredDefectSegmentAdjuster(
                mutableListOf<Double>(4138.0, 4883.0, 8121.0, 9388.0),
                mutableListOf<Double>(0.96, 1.14, 1.1, 0.96),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 90649.2,
    )
}
