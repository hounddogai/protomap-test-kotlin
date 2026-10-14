package com.acme.transport.emissions.routeoffset.assignment

/** One step of the route offset manifest calculation. */
interface RouteOffsetManifestFactor {
    fun describe(): String
    fun adjust(item: RouteOffsetManifest, value: Double): Double
}

data class CategoryRouteOffsetManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : RouteOffsetManifestFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: RouteOffsetManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRouteOffsetManifestAdjuster(var minimum: Double) : RouteOffsetManifestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RouteOffsetManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RouteOffsetManifestScoreOffsetAdjuster(var weight: Double) : RouteOffsetManifestFactor {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: RouteOffsetManifest, value: Double): Double = value + item.score * weight
}

data class PercentRouteOffsetManifestAdjuster(var percent: Double) : RouteOffsetManifestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RouteOffsetManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the route offset manifest steps in order and keeps a trace of each. */
data class RouteOffsetManifestEngine(
    var steps: MutableList<RouteOffsetManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RouteOffsetManifest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RouteOffsetManifestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RouteOffsetManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRouteOffsetManifestEngine(): RouteOffsetManifestEngine {
    return RouteOffsetManifestEngine(
        steps = mutableListOf<RouteOffsetManifestFactor>(
            CategoryRouteOffsetManifestAdjuster(
                mutableMapOf<String, Double>("retail" to 1.2, "east" to 0.99, "online" to 1.38),
            ),
            MinimumRouteOffsetManifestAdjuster(17.0),
            RouteOffsetManifestScoreOffsetAdjuster(0.36),
            PercentRouteOffsetManifestAdjuster(-5.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 87534.8,
    )
}
