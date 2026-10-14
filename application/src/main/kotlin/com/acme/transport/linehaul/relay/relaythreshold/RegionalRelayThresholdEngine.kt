package com.acme.transport.linehaul.relay.relaythreshold

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional relay threshold calculation. */
interface RegionalRelayThresholdStep {
    fun describe(): String
    fun adjust(item: RegionalRelayThreshold, value: Double): Double
}

data class MinimumRegionalRelayThresholdAdjuster(var minimum: Double) : RegionalRelayThresholdStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalRelayThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRegionalRelayThresholdAdjuster(var step: Double) : RegionalRelayThresholdStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalRelayThreshold, value: Double): Double = roundTo(value, step)
}

data class CapRegionalRelayThresholdAdjuster(var cap: Double) : RegionalRelayThresholdStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalRelayThreshold, value: Double): Double = minOf(value, cap)
}

data class ChannelRegionalRelayThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalRelayThresholdStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: RegionalRelayThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRegionalRelayThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalRelayThresholdStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalRelayThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the regional relay threshold steps in order and keeps a trace of each. */
data class RegionalRelayThresholdEngine(
    var steps: MutableList<RegionalRelayThresholdStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalRelayThreshold): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalRelayThresholdStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalRelayThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalRelayThresholdEngine(): RegionalRelayThresholdEngine {
    return RegionalRelayThresholdEngine(
        steps = mutableListOf<RegionalRelayThresholdStep>(
            MinimumRegionalRelayThresholdAdjuster(16.8),
            RoundingRegionalRelayThresholdAdjuster(1.0),
            CapRegionalRelayThresholdAdjuster(7726.0),
            ChannelRegionalRelayThresholdAdjuster(mutableMapOf<String, Double>("south" to 0.75)),
            TieredRegionalRelayThresholdAdjuster(
                mutableListOf<Double>(3178.0, 4800.0, 5784.0, 7472.0),
                mutableListOf<Double>(1.15, 1.19, 1.2, 0.92),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 73114.0,
    )
}
