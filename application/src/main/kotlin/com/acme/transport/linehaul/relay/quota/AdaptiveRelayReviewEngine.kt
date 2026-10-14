package com.acme.transport.linehaul.relay.quota

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive relay review calculation. */
interface AdaptiveRelayReviewAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveRelayReview, value: Double): Double
}

data class AdaptiveRelayReviewVersionOffsetAdjuster(var weight: Double) : AdaptiveRelayReviewAdjuster {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: AdaptiveRelayReview, value: Double): Double = value + item.version.toDouble() * weight
}

data class DepotAdaptiveRelayReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveRelayReviewAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: AdaptiveRelayReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAdaptiveRelayReviewAdjuster(var minimum: Double) : AdaptiveRelayReviewAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveRelayReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapAdaptiveRelayReviewAdjuster(var cap: Double) : AdaptiveRelayReviewAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveRelayReview, value: Double): Double = minOf(value, cap)
}

/** Runs the adaptive relay review steps in order and keeps a trace of each. */
data class AdaptiveRelayReviewEngine(
    var steps: MutableList<AdaptiveRelayReviewAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveRelayReview): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveRelayReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveRelayReviewEngine(): AdaptiveRelayReviewEngine {
    return AdaptiveRelayReviewEngine(
        steps = mutableListOf<AdaptiveRelayReviewAdjuster>(
            AdaptiveRelayReviewVersionOffsetAdjuster(1.32),
            DepotAdaptiveRelayReviewAdjuster(mutableMapOf<String, Double>("east" to 1.3)),
            MinimumAdaptiveRelayReviewAdjuster(24.0),
            CapAdaptiveRelayReviewAdjuster(32429.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 35334.0,
    )
}
