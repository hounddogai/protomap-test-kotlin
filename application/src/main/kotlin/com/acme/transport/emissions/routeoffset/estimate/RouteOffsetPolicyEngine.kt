package com.acme.transport.emissions.routeoffset.estimate

/** One step of the route offset policy calculation. */
interface RouteOffsetPolicyFactor {
    fun describe(): String
    fun adjust(item: RouteOffsetPolicy, value: Double): Double
}

data class RouteOffsetPolicyBatchSizeOffsetAdjuster(var weight: Double) : RouteOffsetPolicyFactor {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: RouteOffsetPolicy, value: Double): Double = value + item.batchSize.toDouble() * weight
}

data class SourceRouteOffsetPolicyAdjuster(var factors: MutableMap<String, Double>) : RouteOffsetPolicyFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: RouteOffsetPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRouteOffsetPolicyAdjuster(var cap: Double) : RouteOffsetPolicyFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RouteOffsetPolicy, value: Double): Double = minOf(value, cap)
}

data class MinimumRouteOffsetPolicyAdjuster(var minimum: Double) : RouteOffsetPolicyFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RouteOffsetPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the route offset policy steps in order and keeps a trace of each. */
data class RouteOffsetPolicyEngine(
    var steps: MutableList<RouteOffsetPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RouteOffsetPolicy): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RouteOffsetPolicyFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RouteOffsetPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRouteOffsetPolicyEngine(): RouteOffsetPolicyEngine {
    return RouteOffsetPolicyEngine(
        steps = mutableListOf<RouteOffsetPolicyFactor>(
            RouteOffsetPolicyBatchSizeOffsetAdjuster(0.28),
            SourceRouteOffsetPolicyAdjuster(
                mutableMapOf<String, Double>("south" to 0.88, "central" to 0.87, "east" to 1.08),
            ),
            CapRouteOffsetPolicyAdjuster(25519.0),
            MinimumRouteOffsetPolicyAdjuster(18.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 47568.7,
    )
}
