package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.kit.numbers.roundTo

/** One step of the route offset review calculation. */
interface RouteOffsetReviewFactor {
    fun describe(): String
    fun adjust(item: RouteOffsetReview, value: Double): Double
}

data class RoundingRouteOffsetReviewAdjuster(var step: Double) : RouteOffsetReviewFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RouteOffsetReview, value: Double): Double = roundTo(value, step)
}

data class RouteOffsetReviewLoadKgOffsetAdjuster(var weight: Double) : RouteOffsetReviewFactor {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: RouteOffsetReview, value: Double): Double = value + item.loadKg * weight
}

data class TieredRouteOffsetReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RouteOffsetReviewFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RouteOffsetReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRouteOffsetReviewAdjuster(var cap: Double) : RouteOffsetReviewFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RouteOffsetReview, value: Double): Double = minOf(value, cap)
}

/** Runs the route offset review steps in order and keeps a trace of each. */
data class RouteOffsetReviewEngine(
    var steps: MutableList<RouteOffsetReviewFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RouteOffsetReview): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RouteOffsetReviewFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RouteOffsetReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRouteOffsetReviewEngine(): RouteOffsetReviewEngine {
    return RouteOffsetReviewEngine(
        steps = mutableListOf<RouteOffsetReviewFactor>(
            RoundingRouteOffsetReviewAdjuster(0.1),
            RouteOffsetReviewLoadKgOffsetAdjuster(0.06),
            TieredRouteOffsetReviewAdjuster(
                mutableListOf<Double>(3405.0, 4405.0, 5057.0, 7863.0),
                mutableListOf<Double>(1.09, 1.07, 1.1, 1.08),
            ),
            CapRouteOffsetReviewAdjuster(13099.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 38513.9,
    )
}
