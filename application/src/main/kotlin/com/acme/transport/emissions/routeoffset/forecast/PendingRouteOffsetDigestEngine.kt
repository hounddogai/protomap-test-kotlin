package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending route offset digest calculation. */
interface PendingRouteOffsetDigestModifier {
    fun describe(): String
    fun adjust(item: PendingRouteOffsetDigest, value: Double): Double
}

data class TitlePendingRouteOffsetDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingRouteOffsetDigestModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: PendingRouteOffsetDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedPendingRouteOffsetDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingRouteOffsetDigestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingRouteOffsetDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingPendingRouteOffsetDigestAdjuster(var step: Double) : PendingRouteOffsetDigestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingRouteOffsetDigest, value: Double): Double = roundTo(value, step)
}

data class TieredPendingRouteOffsetDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingRouteOffsetDigestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingRouteOffsetDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumPendingRouteOffsetDigestAdjuster(var minimum: Double) : PendingRouteOffsetDigestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingRouteOffsetDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the pending route offset digest steps in order and keeps a trace of each. */
data class PendingRouteOffsetDigestEngine(
    var steps: MutableList<PendingRouteOffsetDigestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingRouteOffsetDigest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingRouteOffsetDigestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PendingRouteOffsetDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingRouteOffsetDigestEngine(flags: Registry): PendingRouteOffsetDigestEngine {
    return PendingRouteOffsetDigestEngine(
        steps = mutableListOf<PendingRouteOffsetDigestModifier>(
            TitlePendingRouteOffsetDigestAdjuster(mutableMapOf<String, Double>("retail" to 1.38)),
            FlaggedPendingRouteOffsetDigestAdjuster(flags, "pending-route-offset-digest-pending", 9.1),
            RoundingPendingRouteOffsetDigestAdjuster(0.25),
            TieredPendingRouteOffsetDigestAdjuster(
                mutableListOf<Double>(191.0, 6721.0, 9397.0),
                mutableListOf<Double>(0.89, 0.94, 0.92),
            ),
            MinimumPendingRouteOffsetDigestAdjuster(18.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 98864.8,
    )
}
