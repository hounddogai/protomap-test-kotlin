package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.kit.numbers.roundTo

/** One step of the qualification milestone calculation. */
interface QualificationMilestoneStep {
    fun describe(): String
    fun adjust(item: QualificationMilestone, value: Double): Double
}

data class TieredQualificationMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : QualificationMilestoneStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: QualificationMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LaneQualificationMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : QualificationMilestoneStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: QualificationMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingQualificationMilestoneAdjuster(var step: Double) : QualificationMilestoneStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: QualificationMilestone, value: Double): Double = roundTo(value, step)
}

data class MinimumQualificationMilestoneAdjuster(var minimum: Double) : QualificationMilestoneStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: QualificationMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentQualificationMilestoneAdjuster(var percent: Double) : QualificationMilestoneStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: QualificationMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the qualification milestone steps in order and keeps a trace of each. */
data class QualificationMilestoneEngine(
    var steps: MutableList<QualificationMilestoneStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: QualificationMilestone): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: QualificationMilestoneStep = steps[index]
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

    fun evaluateAll(items: MutableList<QualificationMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultQualificationMilestoneEngine(): QualificationMilestoneEngine {
    return QualificationMilestoneEngine(
        steps = mutableListOf<QualificationMilestoneStep>(
            TieredQualificationMilestoneAdjuster(
                mutableListOf<Double>(311.0, 5244.0, 8178.0),
                mutableListOf<Double>(1.14, 0.92, 0.82),
            ),
            LaneQualificationMilestoneAdjuster(mutableMapOf<String, Double>("central" to 1.15)),
            RoundingQualificationMilestoneAdjuster(0.1),
            MinimumQualificationMilestoneAdjuster(9.7),
            PercentQualificationMilestoneAdjuster(-10.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 61545.9,
    )
}
