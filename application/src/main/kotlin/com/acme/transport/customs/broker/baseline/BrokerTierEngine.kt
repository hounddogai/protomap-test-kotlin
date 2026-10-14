package com.acme.transport.customs.broker.baseline

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the broker tier calculation. */
interface BrokerTierFactor {
    fun describe(): String
    fun adjust(item: BrokerTier, value: Double): Double
}

data class RoundingBrokerTierAdjuster(var step: Double) : BrokerTierFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BrokerTier, value: Double): Double = roundTo(value, step)
}

data class PercentBrokerTierAdjuster(var percent: Double) : BrokerTierFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BrokerTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumBrokerTierAdjuster(var minimum: Double) : BrokerTierFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BrokerTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the broker tier steps in order and keeps a trace of each. */
data class BrokerTierEngine(
    var steps: MutableList<BrokerTierFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BrokerTier): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BrokerTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBrokerTierEngine(): BrokerTierEngine {
    return BrokerTierEngine(
        steps = mutableListOf<BrokerTierFactor>(
            RoundingBrokerTierAdjuster(0.5),
            PercentBrokerTierAdjuster(16.3),
            MinimumBrokerTierAdjuster(36.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 84238.1,
    )
}
