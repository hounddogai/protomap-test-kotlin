package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled accessorial review calculation. */
interface ScheduledAccessorialReviewModifier {
    fun describe(): String
    fun adjust(item: ScheduledAccessorialReview, value: Double): Double
}

data class TieredScheduledAccessorialReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledAccessorialReviewModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledAccessorialReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentScheduledAccessorialReviewAdjuster(var percent: Double) : ScheduledAccessorialReviewModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledAccessorialReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapScheduledAccessorialReviewAdjuster(var cap: Double) : ScheduledAccessorialReviewModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledAccessorialReview, value: Double): Double = minOf(value, cap)
}

data class LabelScheduledAccessorialReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledAccessorialReviewModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: ScheduledAccessorialReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scheduled accessorial review steps in order and keeps a trace of each. */
data class ScheduledAccessorialReviewEngine(
    var steps: MutableList<ScheduledAccessorialReviewModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledAccessorialReview): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledAccessorialReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledAccessorialReviewEngine(): ScheduledAccessorialReviewEngine {
    return ScheduledAccessorialReviewEngine(
        steps = mutableListOf<ScheduledAccessorialReviewModifier>(
            TieredScheduledAccessorialReviewAdjuster(
                mutableListOf<Double>(126.0, 603.0, 2344.0, 9226.0),
                mutableListOf<Double>(1.17, 1.18, 1.08, 1.02),
            ),
            PercentScheduledAccessorialReviewAdjuster(18.9),
            CapScheduledAccessorialReviewAdjuster(7455.0),
            LabelScheduledAccessorialReviewAdjuster(
                mutableMapOf<String, Double>("retail" to 1.34, "central" to 0.94),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 54150.0,
    )
}
