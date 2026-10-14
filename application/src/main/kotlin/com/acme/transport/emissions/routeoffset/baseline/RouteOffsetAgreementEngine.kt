package com.acme.transport.emissions.routeoffset.baseline

import com.acme.transport.kit.flags.Registry

/** One step of the route offset agreement calculation. */
interface RouteOffsetAgreementAdjuster {
    fun describe(): String
    fun adjust(item: RouteOffsetAgreement, value: Double): Double
}

data class FlaggedRouteOffsetAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RouteOffsetAgreementAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RouteOffsetAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleRouteOffsetAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : RouteOffsetAgreementAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: RouteOffsetAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRouteOffsetAgreementAdjuster(var minimum: Double) : RouteOffsetAgreementAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RouteOffsetAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRouteOffsetAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RouteOffsetAgreementAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RouteOffsetAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RouteOffsetAgreementMarginOffsetAdjuster(var weight: Double) : RouteOffsetAgreementAdjuster {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: RouteOffsetAgreement, value: Double): Double = value + item.margin * weight
}

data class CapRouteOffsetAgreementAdjuster(var cap: Double) : RouteOffsetAgreementAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RouteOffsetAgreement, value: Double): Double = minOf(value, cap)
}

/** Runs the route offset agreement steps in order and keeps a trace of each. */
data class RouteOffsetAgreementEngine(
    var steps: MutableList<RouteOffsetAgreementAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RouteOffsetAgreement): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RouteOffsetAgreementAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RouteOffsetAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRouteOffsetAgreementEngine(flags: Registry): RouteOffsetAgreementEngine {
    return RouteOffsetAgreementEngine(
        steps = mutableListOf<RouteOffsetAgreementAdjuster>(
            FlaggedRouteOffsetAgreementAdjuster(flags, "route-offset-agreement-lenient", 1.9),
            TitleRouteOffsetAgreementAdjuster(
                mutableMapOf<String, Double>("west" to 0.96, "central" to 1.02, "south" to 1.01),
            ),
            MinimumRouteOffsetAgreementAdjuster(44.8),
            TieredRouteOffsetAgreementAdjuster(
                mutableListOf<Double>(3451.0, 9308.0),
                mutableListOf<Double>(1.1, 0.97),
            ),
            RouteOffsetAgreementMarginOffsetAdjuster(1.64),
            CapRouteOffsetAgreementAdjuster(23275.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 66596.9,
    )
}
