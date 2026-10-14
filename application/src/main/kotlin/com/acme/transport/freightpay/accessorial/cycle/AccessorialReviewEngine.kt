package com.acme.transport.freightpay.accessorial.cycle

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the accessorial review calculation. */
interface AccessorialReviewStep {
    fun describe(): String
    fun adjust(item: AccessorialReview, value: Double): Double
}

data class AccessorialReviewMaximumOffsetAdjuster(var weight: Double) : AccessorialReviewStep {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: AccessorialReview, value: Double): Double = value + item.maximum * weight
}

data class PercentAccessorialReviewAdjuster(var percent: Double) : AccessorialReviewStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AccessorialReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleAccessorialReviewAdjuster(var factors: MutableMap<String, Double>) : AccessorialReviewStep {
    override fun describe(): String = "by title"

    override fun adjust(item: AccessorialReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredAccessorialReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AccessorialReviewStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AccessorialReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumAccessorialReviewAdjuster(var minimum: Double) : AccessorialReviewStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AccessorialReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the accessorial review steps in order and keeps a trace of each. */
data class AccessorialReviewEngine(
    var steps: MutableList<AccessorialReviewStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AccessorialReview): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AccessorialReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAccessorialReviewEngine(): AccessorialReviewEngine {
    return AccessorialReviewEngine(
        steps = mutableListOf<AccessorialReviewStep>(
            AccessorialReviewMaximumOffsetAdjuster(0.34),
            PercentAccessorialReviewAdjuster(-24.4),
            TitleAccessorialReviewAdjuster(
                mutableMapOf<String, Double>("north" to 1.13, "central" to 1.05, "retail" to 0.78),
            ),
            TieredAccessorialReviewAdjuster(
                mutableListOf<Double>(1981.0, 4905.0),
                mutableListOf<Double>(1.09, 0.93),
            ),
            MinimumAccessorialReviewAdjuster(0.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 67417.3,
    )
}
