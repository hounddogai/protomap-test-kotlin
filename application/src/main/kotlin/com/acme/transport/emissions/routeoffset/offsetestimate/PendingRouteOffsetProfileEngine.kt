package com.acme.transport.emissions.routeoffset.offsetestimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending route offset profile calculation. */
interface PendingRouteOffsetProfileStep {
    fun describe(): String
    fun adjust(item: PendingRouteOffsetProfile, value: Double): Double
}

data class RoundingPendingRouteOffsetProfileAdjuster(var step: Double) : PendingRouteOffsetProfileStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingRouteOffsetProfile, value: Double): Double = roundTo(value, step)
}

data class MinimumPendingRouteOffsetProfileAdjuster(var minimum: Double) : PendingRouteOffsetProfileStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingRouteOffsetProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitlePendingRouteOffsetProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingRouteOffsetProfileStep {
    override fun describe(): String = "by title"

    override fun adjust(item: PendingRouteOffsetProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapPendingRouteOffsetProfileAdjuster(var cap: Double) : PendingRouteOffsetProfileStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingRouteOffsetProfile, value: Double): Double = minOf(value, cap)
}

data class FlaggedPendingRouteOffsetProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingRouteOffsetProfileStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingRouteOffsetProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PendingRouteOffsetProfileLimitOffsetAdjuster(var weight: Double) : PendingRouteOffsetProfileStep {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: PendingRouteOffsetProfile, value: Double): Double {
        return value + item.limit.toDouble() * weight
    }
}

/** Runs the pending route offset profile steps in order and keeps a trace of each. */
data class PendingRouteOffsetProfileEngine(
    var steps: MutableList<PendingRouteOffsetProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingRouteOffsetProfile): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingRouteOffsetProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingRouteOffsetProfileEngine(flags: Registry): PendingRouteOffsetProfileEngine {
    return PendingRouteOffsetProfileEngine(
        steps = mutableListOf<PendingRouteOffsetProfileStep>(
            RoundingPendingRouteOffsetProfileAdjuster(1.0),
            MinimumPendingRouteOffsetProfileAdjuster(46.1),
            TitlePendingRouteOffsetProfileAdjuster(
                mutableMapOf<String, Double>("north" to 1.27, "online" to 0.84),
            ),
            CapPendingRouteOffsetProfileAdjuster(16178.0),
            FlaggedPendingRouteOffsetProfileAdjuster(flags, "pending-route-offset-profile-manual", 3.8),
            PendingRouteOffsetProfileLimitOffsetAdjuster(0.08),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 27914.1,
    )
}
