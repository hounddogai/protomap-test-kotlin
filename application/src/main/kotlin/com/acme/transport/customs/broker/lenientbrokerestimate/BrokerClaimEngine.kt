package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.kit.numbers.roundTo

/** One step of the broker claim calculation. */
interface BrokerClaimStep {
    fun describe(): String
    fun adjust(item: BrokerClaim, value: Double): Double
}

data class BrokerClaimLoadKgOffsetAdjuster(var weight: Double) : BrokerClaimStep {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: BrokerClaim, value: Double): Double = value + item.loadKg * weight
}

data class MinimumBrokerClaimAdjuster(var minimum: Double) : BrokerClaimStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BrokerClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingBrokerClaimAdjuster(var step: Double) : BrokerClaimStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BrokerClaim, value: Double): Double = roundTo(value, step)
}

data class PercentBrokerClaimAdjuster(var percent: Double) : BrokerClaimStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BrokerClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the broker claim steps in order and keeps a trace of each. */
data class BrokerClaimEngine(
    var steps: MutableList<BrokerClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BrokerClaim): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BrokerClaimStep = steps[index]
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

    fun evaluateAll(items: MutableList<BrokerClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBrokerClaimEngine(): BrokerClaimEngine {
    return BrokerClaimEngine(
        steps = mutableListOf<BrokerClaimStep>(
            BrokerClaimLoadKgOffsetAdjuster(1.72),
            MinimumBrokerClaimAdjuster(29.0),
            RoundingBrokerClaimAdjuster(0.1),
            PercentBrokerClaimAdjuster(20.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 60013.2,
    )
}
