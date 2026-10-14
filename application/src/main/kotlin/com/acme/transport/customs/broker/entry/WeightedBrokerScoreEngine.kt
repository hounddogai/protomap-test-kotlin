package com.acme.transport.customs.broker.entry

/** One step of the weighted broker score calculation. */
interface WeightedBrokerScoreFactor {
    fun describe(): String
    fun adjust(item: WeightedBrokerScore, value: Double): Double
}

data class MinimumWeightedBrokerScoreAdjuster(var minimum: Double) : WeightedBrokerScoreFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedBrokerScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredWeightedBrokerScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedBrokerScoreFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedBrokerScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapWeightedBrokerScoreAdjuster(var cap: Double) : WeightedBrokerScoreFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedBrokerScore, value: Double): Double = minOf(value, cap)
}

data class PercentWeightedBrokerScoreAdjuster(var percent: Double) : WeightedBrokerScoreFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedBrokerScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the weighted broker score steps in order and keeps a trace of each. */
data class WeightedBrokerScoreEngine(
    var steps: MutableList<WeightedBrokerScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedBrokerScore): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedBrokerScoreFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedBrokerScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedBrokerScoreEngine(): WeightedBrokerScoreEngine {
    return WeightedBrokerScoreEngine(
        steps = mutableListOf<WeightedBrokerScoreFactor>(
            MinimumWeightedBrokerScoreAdjuster(27.2),
            TieredWeightedBrokerScoreAdjuster(
                mutableListOf<Double>(3485.0, 4438.0),
                mutableListOf<Double>(1.06, 0.93),
            ),
            CapWeightedBrokerScoreAdjuster(8068.0),
            PercentWeightedBrokerScoreAdjuster(1.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 92505.2,
    )
}
