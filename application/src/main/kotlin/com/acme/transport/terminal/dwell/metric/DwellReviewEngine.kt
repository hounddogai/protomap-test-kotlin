package com.acme.transport.terminal.dwell.metric

import com.acme.transport.kit.flags.Registry

/** One step of the dwell review calculation. */
interface DwellReviewModifier {
    fun describe(): String
    fun adjust(item: DwellReview, value: Double): Double
}

data class PercentDwellReviewAdjuster(var percent: Double) : DwellReviewModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DwellReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDwellReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DwellReviewModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DwellReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedDwellReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DwellReviewModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DwellReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DwellReviewPriorityOffsetAdjuster(var weight: Double) : DwellReviewModifier {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: DwellReview, value: Double): Double = value + item.priority.toDouble() * weight
}

/** Runs the dwell review steps in order and keeps a trace of each. */
data class DwellReviewEngine(
    var steps: MutableList<DwellReviewModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DwellReview): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DwellReviewModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DwellReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDwellReviewEngine(flags: Registry): DwellReviewEngine {
    return DwellReviewEngine(
        steps = mutableListOf<DwellReviewModifier>(
            PercentDwellReviewAdjuster(-12.1),
            TieredDwellReviewAdjuster(
                mutableListOf<Double>(6911.0, 7469.0, 7987.0),
                mutableListOf<Double>(1.12, 0.9, 0.89),
            ),
            FlaggedDwellReviewAdjuster(flags, "dwell-review-manual", 4.0),
            DwellReviewPriorityOffsetAdjuster(0.06),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 71536.7,
    )
}
