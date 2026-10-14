package com.acme.transport.emissions.routeoffset.offsetbaseline

import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled route offset allocation calculation. */
interface ScheduledRouteOffsetAllocationStep {
    fun describe(): String
    fun adjust(item: ScheduledRouteOffsetAllocation, value: Double): Double
}

data class RoundingScheduledRouteOffsetAllocationAdjuster(
    var step: Double,
) : ScheduledRouteOffsetAllocationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledRouteOffsetAllocation, value: Double): Double = roundTo(value, step)
}

data class TieredScheduledRouteOffsetAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledRouteOffsetAllocationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledRouteOffsetAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentScheduledRouteOffsetAllocationAdjuster(
    var percent: Double,
) : ScheduledRouteOffsetAllocationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledRouteOffsetAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ChannelScheduledRouteOffsetAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledRouteOffsetAllocationStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: ScheduledRouteOffsetAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumScheduledRouteOffsetAllocationAdjuster(
    var minimum: Double,
) : ScheduledRouteOffsetAllocationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledRouteOffsetAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the scheduled route offset allocation steps in order and keeps a trace of each. */
data class ScheduledRouteOffsetAllocationEngine(
    var steps: MutableList<ScheduledRouteOffsetAllocationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledRouteOffsetAllocation): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledRouteOffsetAllocationStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledRouteOffsetAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledRouteOffsetAllocationEngine(): ScheduledRouteOffsetAllocationEngine {
    return ScheduledRouteOffsetAllocationEngine(
        steps = mutableListOf<ScheduledRouteOffsetAllocationStep>(
            RoundingScheduledRouteOffsetAllocationAdjuster(0.5),
            TieredScheduledRouteOffsetAllocationAdjuster(
                mutableListOf<Double>(2278.0, 4790.0, 7105.0, 8577.0),
                mutableListOf<Double>(0.93, 1.03, 1.01, 1.1),
            ),
            PercentScheduledRouteOffsetAllocationAdjuster(-1.8),
            ChannelScheduledRouteOffsetAllocationAdjuster(mutableMapOf<String, Double>("retail" to 1.12)),
            MinimumScheduledRouteOffsetAllocationAdjuster(24.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 2001.9,
    )
}
