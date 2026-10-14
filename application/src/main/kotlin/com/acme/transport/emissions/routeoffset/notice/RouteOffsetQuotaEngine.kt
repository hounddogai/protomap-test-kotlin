package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the route offset quota calculation. */
interface RouteOffsetQuotaStep {
    fun describe(): String
    fun adjust(item: RouteOffsetQuota, value: Double): Double
}

data class CapRouteOffsetQuotaAdjuster(var cap: Double) : RouteOffsetQuotaStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RouteOffsetQuota, value: Double): Double = minOf(value, cap)
}

data class FlaggedRouteOffsetQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RouteOffsetQuotaStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RouteOffsetQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRouteOffsetQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RouteOffsetQuotaStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RouteOffsetQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRouteOffsetQuotaAdjuster(var percent: Double) : RouteOffsetQuotaStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RouteOffsetQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the route offset quota steps in order and keeps a trace of each. */
data class RouteOffsetQuotaEngine(
    var steps: MutableList<RouteOffsetQuotaStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RouteOffsetQuota): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RouteOffsetQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRouteOffsetQuotaEngine(flags: Registry): RouteOffsetQuotaEngine {
    return RouteOffsetQuotaEngine(
        steps = mutableListOf<RouteOffsetQuotaStep>(
            CapRouteOffsetQuotaAdjuster(32516.0),
            FlaggedRouteOffsetQuotaAdjuster(flags, "route-offset-quota-legacy", -8.3),
            TieredRouteOffsetQuotaAdjuster(
                mutableListOf<Double>(4719.0, 8723.0),
                mutableListOf<Double>(1.02, 0.99),
            ),
            PercentRouteOffsetQuotaAdjuster(-5.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 64823.2,
    )
}
