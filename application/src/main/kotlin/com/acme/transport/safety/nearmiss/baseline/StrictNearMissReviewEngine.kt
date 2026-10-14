package com.acme.transport.safety.nearmiss.baseline

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict near miss review calculation. */
interface StrictNearMissReviewStep {
    fun describe(): String
    fun adjust(item: StrictNearMissReview, value: Double): Double
}

data class CapStrictNearMissReviewAdjuster(var cap: Double) : StrictNearMissReviewStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictNearMissReview, value: Double): Double = minOf(value, cap)
}

data class PercentStrictNearMissReviewAdjuster(var percent: Double) : StrictNearMissReviewStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictNearMissReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredStrictNearMissReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictNearMissReviewStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictNearMissReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelStrictNearMissReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictNearMissReviewStep {
    override fun describe(): String = "by label"

    override fun adjust(item: StrictNearMissReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the strict near miss review steps in order and keeps a trace of each. */
data class StrictNearMissReviewEngine(
    var steps: MutableList<StrictNearMissReviewStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictNearMissReview): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictNearMissReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictNearMissReviewEngine(): StrictNearMissReviewEngine {
    return StrictNearMissReviewEngine(
        steps = mutableListOf<StrictNearMissReviewStep>(
            CapStrictNearMissReviewAdjuster(3586.0),
            PercentStrictNearMissReviewAdjuster(10.4),
            TieredStrictNearMissReviewAdjuster(
                mutableListOf<Double>(1107.0, 1232.0, 2040.0, 7529.0),
                mutableListOf<Double>(1.17, 0.96, 1.15, 1.09),
            ),
            LabelStrictNearMissReviewAdjuster(mutableMapOf<String, Double>("east" to 1.07, "west" to 1.15)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 99202.7,
    )
}
