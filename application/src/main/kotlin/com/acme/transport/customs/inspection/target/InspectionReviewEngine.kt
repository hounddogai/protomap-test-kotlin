package com.acme.transport.customs.inspection.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the inspection review calculation. */
interface InspectionReviewAdjuster {
    fun describe(): String
    fun adjust(item: InspectionReview, value: Double): Double
}

data class PercentInspectionReviewAdjuster(var percent: Double) : InspectionReviewAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: InspectionReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedInspectionReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionReviewAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapInspectionReviewAdjuster(var cap: Double) : InspectionReviewAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionReview, value: Double): Double = minOf(value, cap)
}

/** Runs the inspection review steps in order and keeps a trace of each. */
data class InspectionReviewEngine(
    var steps: MutableList<InspectionReviewAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: InspectionReview): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
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
        steps = mutableListOf<InspectionReviewAdjuster>(
            PercentInspectionReviewAdjuster(-18.6),
            FlaggedInspectionReviewAdjuster(flags, "inspection-review-fallback", -5.7),
            CapInspectionReviewAdjuster(16492.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 6737.7,
    )
}
