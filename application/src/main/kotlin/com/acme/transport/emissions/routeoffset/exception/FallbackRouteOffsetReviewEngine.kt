package com.acme.transport.emissions.routeoffset.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback route offset review calculation. */
interface FallbackRouteOffsetReviewModifier {
    fun describe(): String
    fun adjust(item: FallbackRouteOffsetReview, value: Double): Double
}

data class UnitFallbackRouteOffsetReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackRouteOffsetReviewModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: FallbackRouteOffsetReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredFallbackRouteOffsetReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackRouteOffsetReviewModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackRouteOffsetReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedFallbackRouteOffsetReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackRouteOffsetReviewModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackRouteOffsetReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the fallback route offset review steps in order and keeps a trace of each. */
data class FallbackRouteOffsetReviewEngine(
    var steps: MutableList<FallbackRouteOffsetReviewModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackRouteOffsetReview): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackRouteOffsetReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackRouteOffsetReviewEngine(flags: Registry): FallbackRouteOffsetReviewEngine {
    return FallbackRouteOffsetReviewEngine(
        steps = mutableListOf<FallbackRouteOffsetReviewModifier>(
            UnitFallbackRouteOffsetReviewAdjuster(
                mutableMapOf<String, Double>("east" to 1.23, "online" to 0.76),
            ),
            TieredFallbackRouteOffsetReviewAdjuster(
                mutableListOf<Double>(128.0, 5260.0, 9308.0, 9895.0),
                mutableListOf<Double>(1.1, 0.87, 1.06, 1.19),
            ),
            FlaggedFallbackRouteOffsetReviewAdjuster(flags, "fallback-route-offset-review-default", -3.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 84959.7,
    )
}
