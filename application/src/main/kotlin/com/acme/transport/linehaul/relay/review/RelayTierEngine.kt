package com.acme.transport.linehaul.relay.review

import com.acme.transport.kit.numbers.roundTo

/** One step of the relay tier calculation. */
interface RelayTierFactor {
    fun describe(): String
    fun adjust(item: RelayTier, value: Double): Double
}

data class CountryRelayTierAdjuster(var factors: MutableMap<String, Double>) : RelayTierFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: RelayTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRelayTierAdjuster(var step: Double) : RelayTierFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RelayTier, value: Double): Double = roundTo(value, step)
}

data class RelayTierStopCountOffsetAdjuster(var weight: Double) : RelayTierFactor {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: RelayTier, value: Double): Double = value + item.stopCount.toDouble() * weight
}

/** Runs the relay tier steps in order and keeps a trace of each. */
data class RelayTierEngine(
    var steps: MutableList<RelayTierFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RelayTier): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RelayTierFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RelayTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRelayTierEngine(): RelayTierEngine {
    return RelayTierEngine(
        steps = mutableListOf<RelayTierFactor>(
            CountryRelayTierAdjuster(mutableMapOf<String, Double>("retail" to 1.12, "central" to 1.37)),
            RoundingRelayTierAdjuster(0.1),
            RelayTierStopCountOffsetAdjuster(0.89),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 63274.7,
    )
}
