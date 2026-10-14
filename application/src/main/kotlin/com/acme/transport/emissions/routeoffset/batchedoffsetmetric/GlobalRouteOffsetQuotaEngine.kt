package com.acme.transport.emissions.routeoffset.batchedoffsetmetric

import com.acme.transport.kit.numbers.roundTo

/** One step of the global route offset quota calculation. */
interface GlobalRouteOffsetQuotaAdjuster {
    fun describe(): String
    fun adjust(item: GlobalRouteOffsetQuota, value: Double): Double
}

data class MinimumGlobalRouteOffsetQuotaAdjuster(var minimum: Double) : GlobalRouteOffsetQuotaAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalRouteOffsetQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingGlobalRouteOffsetQuotaAdjuster(var step: Double) : GlobalRouteOffsetQuotaAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalRouteOffsetQuota, value: Double): Double = roundTo(value, step)
}

data class GlobalRouteOffsetQuotaEndsAtOffsetAdjuster(var weight: Double) : GlobalRouteOffsetQuotaAdjuster {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: GlobalRouteOffsetQuota, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class LaneGlobalRouteOffsetQuotaAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalRouteOffsetQuotaAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: GlobalRouteOffsetQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the global route offset quota steps in order and keeps a trace of each. */
data class GlobalRouteOffsetQuotaEngine(
    var steps: MutableList<GlobalRouteOffsetQuotaAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalRouteOffsetQuota): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalRouteOffsetQuotaAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalRouteOffsetQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalRouteOffsetQuotaEngine(): GlobalRouteOffsetQuotaEngine {
    return GlobalRouteOffsetQuotaEngine(
        steps = mutableListOf<GlobalRouteOffsetQuotaAdjuster>(
            MinimumGlobalRouteOffsetQuotaAdjuster(31.0),
            RoundingGlobalRouteOffsetQuotaAdjuster(0.05),
            GlobalRouteOffsetQuotaEndsAtOffsetAdjuster(1.9),
            LaneGlobalRouteOffsetQuotaAdjuster(mutableMapOf<String, Double>("west" to 1.03)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 11678.6,
    )
}
