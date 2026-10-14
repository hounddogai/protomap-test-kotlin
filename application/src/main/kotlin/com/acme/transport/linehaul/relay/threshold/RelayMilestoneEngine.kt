package com.acme.transport.linehaul.relay.threshold

import com.acme.transport.kit.numbers.roundTo

/** One step of the relay milestone calculation. */
interface RelayMilestoneAdjuster {
    fun describe(): String
    fun adjust(item: RelayMilestone, value: Double): Double
}

data class TieredRelayMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RelayMilestoneAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RelayMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRelayMilestoneAdjuster(var cap: Double) : RelayMilestoneAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RelayMilestone, value: Double): Double = minOf(value, cap)
}

data class NameRelayMilestoneAdjuster(var factors: MutableMap<String, Double>) : RelayMilestoneAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: RelayMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRelayMilestoneAdjuster(var step: Double) : RelayMilestoneAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RelayMilestone, value: Double): Double = roundTo(value, step)
}

data class MinimumRelayMilestoneAdjuster(var minimum: Double) : RelayMilestoneAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RelayMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRelayMilestoneAdjuster(var percent: Double) : RelayMilestoneAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RelayMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the relay milestone steps in order and keeps a trace of each. */
data class RelayMilestoneEngine(
    var steps: MutableList<RelayMilestoneAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RelayMilestone): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RelayMilestoneAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RelayMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRelayMilestoneEngine(): RelayMilestoneEngine {
    return RelayMilestoneEngine(
        steps = mutableListOf<RelayMilestoneAdjuster>(
            TieredRelayMilestoneAdjuster(
                mutableListOf<Double>(2759.0, 4523.0),
                mutableListOf<Double>(1.14, 1.03),
            ),
            CapRelayMilestoneAdjuster(20315.0),
            NameRelayMilestoneAdjuster(mutableMapOf<String, Double>("central" to 1.06, "west" to 0.94)),
            RoundingRelayMilestoneAdjuster(1.0),
            MinimumRelayMilestoneAdjuster(10.3),
            PercentRelayMilestoneAdjuster(-10.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 37496.1,
    )
}
