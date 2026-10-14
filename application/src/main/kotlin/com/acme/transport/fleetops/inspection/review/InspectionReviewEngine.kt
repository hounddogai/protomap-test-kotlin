package com.acme.transport.fleetops.inspection.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the inspection review calculation. */
interface InspectionReviewModifier {
    fun describe(): String
    fun adjust(item: InspectionReview, value: Double): Double
}

data class CapInspectionReviewAdjuster(var cap: Double) : InspectionReviewModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionReview, value: Double): Double = minOf(value, cap)
}

data class FlaggedInspectionReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionReviewModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredInspectionReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionReviewModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumInspectionReviewAdjuster(var minimum: Double) : InspectionReviewModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingInspectionReviewAdjuster(var step: Double) : InspectionReviewModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionReview, value: Double): Double = roundTo(value, step)
}

/** Runs the inspection review steps in order and keeps a trace of each. */
data class InspectionReviewEngine(
    var steps: MutableList<InspectionReviewModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: InspectionReview): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionReviewModifier = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultInspectionReviewEngine(flags: Registry): InspectionReviewEngine {
    return InspectionReviewEngine(
        steps = mutableListOf<InspectionReviewModifier>(
            CapInspectionReviewAdjuster(13245.0),
            FlaggedInspectionReviewAdjuster(flags, "inspection-review-lenient", 3.7),
            TieredInspectionReviewAdjuster(
                mutableListOf<Double>(3902.0, 4507.0, 5550.0, 8070.0),
                mutableListOf<Double>(0.94, 1.17, 1.05, 1.14),
            ),
            MinimumInspectionReviewAdjuster(5.8),
            RoundingInspectionReviewAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 37827.8,
    )
}
