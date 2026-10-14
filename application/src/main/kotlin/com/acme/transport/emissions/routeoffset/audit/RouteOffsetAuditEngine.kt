package com.acme.transport.emissions.routeoffset.audit

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the route offset audit calculation. */
interface RouteOffsetAuditFactor {
    fun describe(): String
    fun adjust(item: RouteOffsetAudit, value: Double): Double
}

data class LabelRouteOffsetAuditAdjuster(var factors: MutableMap<String, Double>) : RouteOffsetAuditFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: RouteOffsetAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRouteOffsetAuditAdjuster(var step: Double) : RouteOffsetAuditFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RouteOffsetAudit, value: Double): Double = roundTo(value, step)
}

data class TieredRouteOffsetAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RouteOffsetAuditFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RouteOffsetAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRouteOffsetAuditAdjuster(var cap: Double) : RouteOffsetAuditFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RouteOffsetAudit, value: Double): Double = minOf(value, cap)
}

data class RouteOffsetAuditOdometerOffsetAdjuster(var weight: Double) : RouteOffsetAuditFactor {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: RouteOffsetAudit, value: Double): Double = value + item.odometer.toDouble() * weight
}

/** Runs the route offset audit steps in order and keeps a trace of each. */
data class RouteOffsetAuditEngine(
    var steps: MutableList<RouteOffsetAuditFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RouteOffsetAudit): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RouteOffsetAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRouteOffsetAuditEngine(): RouteOffsetAuditEngine {
    return RouteOffsetAuditEngine(
        steps = mutableListOf<RouteOffsetAuditFactor>(
            LabelRouteOffsetAuditAdjuster(
                mutableMapOf<String, Double>("south" to 1.18, "north" to 0.86, "central" to 1.08),
            ),
            RoundingRouteOffsetAuditAdjuster(0.5),
            TieredRouteOffsetAuditAdjuster(
                mutableListOf<Double>(5028.0, 6161.0, 6558.0, 8903.0),
                mutableListOf<Double>(1.19, 0.81, 1.12, 1.12),
            ),
            CapRouteOffsetAuditAdjuster(26628.0),
            RouteOffsetAuditOdometerOffsetAdjuster(0.72),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 68207.6,
    )
}
