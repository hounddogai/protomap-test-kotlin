package com.acme.transport.customs.broker.profile

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional broker checkpoint calculation. */
interface RegionalBrokerCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: RegionalBrokerCheckpoint, value: Double): Double
}

data class CapRegionalBrokerCheckpointAdjuster(var cap: Double) : RegionalBrokerCheckpointAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalBrokerCheckpoint, value: Double): Double = minOf(value, cap)
}

data class TieredRegionalBrokerCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalBrokerCheckpointAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalBrokerCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRegionalBrokerCheckpointAdjuster(var step: Double) : RegionalBrokerCheckpointAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalBrokerCheckpoint, value: Double): Double = roundTo(value, step)
}

data class MinimumRegionalBrokerCheckpointAdjuster(var minimum: Double) : RegionalBrokerCheckpointAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalBrokerCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRegionalBrokerCheckpointAdjuster(var percent: Double) : RegionalBrokerCheckpointAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalBrokerCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the regional broker checkpoint steps in order and keeps a trace of each. */
data class RegionalBrokerCheckpointEngine(
    var steps: MutableList<RegionalBrokerCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalBrokerCheckpoint): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalBrokerCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalBrokerCheckpointEngine(): RegionalBrokerCheckpointEngine {
    return RegionalBrokerCheckpointEngine(
        steps = mutableListOf<RegionalBrokerCheckpointAdjuster>(
            CapRegionalBrokerCheckpointAdjuster(47778.0),
            TieredRegionalBrokerCheckpointAdjuster(
                mutableListOf<Double>(2059.0, 5066.0, 8742.0, 9171.0),
                mutableListOf<Double>(1.0, 1.07, 0.86, 1.09),
            ),
            RoundingRegionalBrokerCheckpointAdjuster(0.1),
            MinimumRegionalBrokerCheckpointAdjuster(38.5),
            PercentRegionalBrokerCheckpointAdjuster(-0.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 3257.9,
    )
}
