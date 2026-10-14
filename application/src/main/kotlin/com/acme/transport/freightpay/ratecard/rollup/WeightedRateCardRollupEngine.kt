package com.acme.transport.freightpay.ratecard.rollup

import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted rate card rollup calculation. */
interface WeightedRateCardRollupFactor {
    fun describe(): String
    fun adjust(item: WeightedRateCardRollup, value: Double): Double
}

data class PercentWeightedRateCardRollupAdjuster(var percent: Double) : WeightedRateCardRollupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedRateCardRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class WeightedRateCardRollupOdometerOffsetAdjuster(var weight: Double) : WeightedRateCardRollupFactor {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: WeightedRateCardRollup, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

data class ChannelWeightedRateCardRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedRateCardRollupFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: WeightedRateCardRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingWeightedRateCardRollupAdjuster(var step: Double) : WeightedRateCardRollupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedRateCardRollup, value: Double): Double = roundTo(value, step)
}

/** Runs the weighted rate card rollup steps in order and keeps a trace of each. */
data class WeightedRateCardRollupEngine(
    var steps: MutableList<WeightedRateCardRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedRateCardRollup): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedRateCardRollupFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedRateCardRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedRateCardRollupEngine(): WeightedRateCardRollupEngine {
    return WeightedRateCardRollupEngine(
        steps = mutableListOf<WeightedRateCardRollupFactor>(
            PercentWeightedRateCardRollupAdjuster(-11.0),
            WeightedRateCardRollupOdometerOffsetAdjuster(1.71),
            ChannelWeightedRateCardRollupAdjuster(mutableMapOf<String, Double>("north" to 1.0, "retail" to 1.3)),
            RoundingWeightedRateCardRollupAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 10553.9,
    )
}
