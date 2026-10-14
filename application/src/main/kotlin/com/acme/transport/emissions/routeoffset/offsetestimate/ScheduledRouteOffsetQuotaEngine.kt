package com.acme.transport.emissions.routeoffset.offsetestimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled route offset quota calculation. */
interface ScheduledRouteOffsetQuotaFactor {
    fun describe(): String
    fun adjust(item: ScheduledRouteOffsetQuota, value: Double): Double
}

data class ScheduledRouteOffsetQuotaStopCountOffsetAdjuster(
    var weight: Double,
) : ScheduledRouteOffsetQuotaFactor {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: ScheduledRouteOffsetQuota, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

data class MinimumScheduledRouteOffsetQuotaAdjuster(var minimum: Double) : ScheduledRouteOffsetQuotaFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledRouteOffsetQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingScheduledRouteOffsetQuotaAdjuster(var step: Double) : ScheduledRouteOffsetQuotaFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledRouteOffsetQuota, value: Double): Double = roundTo(value, step)
}

data class TieredScheduledRouteOffsetQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledRouteOffsetQuotaFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledRouteOffsetQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentScheduledRouteOffsetQuotaAdjuster(var percent: Double) : ScheduledRouteOffsetQuotaFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledRouteOffsetQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedScheduledRouteOffsetQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledRouteOffsetQuotaFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledRouteOffsetQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the scheduled route offset quota steps in order and keeps a trace of each. */
data class ScheduledRouteOffsetQuotaEngine(
    var steps: MutableList<ScheduledRouteOffsetQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledRouteOffsetQuota): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledRouteOffsetQuotaFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledRouteOffsetQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledRouteOffsetQuotaEngine(flags: Registry): ScheduledRouteOffsetQuotaEngine {
    return ScheduledRouteOffsetQuotaEngine(
        steps = mutableListOf<ScheduledRouteOffsetQuotaFactor>(
            ScheduledRouteOffsetQuotaStopCountOffsetAdjuster(1.84),
            MinimumScheduledRouteOffsetQuotaAdjuster(5.2),
            RoundingScheduledRouteOffsetQuotaAdjuster(0.05),
            TieredScheduledRouteOffsetQuotaAdjuster(
                mutableListOf<Double>(2188.0, 2286.0, 5824.0, 8985.0),
                mutableListOf<Double>(1.0, 0.95, 1.1, 1.01),
            ),
            PercentScheduledRouteOffsetQuotaAdjuster(-21.2),
            FlaggedScheduledRouteOffsetQuotaAdjuster(flags, "scheduled-route-offset-quota-scheduled", -0.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 59558.5,
    )
}
