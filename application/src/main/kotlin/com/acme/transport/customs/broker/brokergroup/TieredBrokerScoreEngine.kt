package com.acme.transport.customs.broker.brokergroup

import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered broker score calculation. */
interface TieredBrokerScoreModifier {
    fun describe(): String
    fun adjust(item: TieredBrokerScore, value: Double): Double
}

data class RoundingTieredBrokerScoreAdjuster(var step: Double) : TieredBrokerScoreModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredBrokerScore, value: Double): Double = roundTo(value, step)
}

data class PercentTieredBrokerScoreAdjuster(var percent: Double) : TieredBrokerScoreModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredBrokerScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleTieredBrokerScoreAdjuster(var factors: MutableMap<String, Double>) : TieredBrokerScoreModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: TieredBrokerScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTieredBrokerScoreAdjuster(var minimum: Double) : TieredBrokerScoreModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredBrokerScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the tiered broker score steps in order and keeps a trace of each. */
data class TieredBrokerScoreEngine(
    var steps: MutableList<TieredBrokerScoreModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredBrokerScore): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredBrokerScoreModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TieredBrokerScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredBrokerScoreEngine(): TieredBrokerScoreEngine {
    return TieredBrokerScoreEngine(
        steps = mutableListOf<TieredBrokerScoreModifier>(
            RoundingTieredBrokerScoreAdjuster(0.1),
            PercentTieredBrokerScoreAdjuster(15.3),
            TitleTieredBrokerScoreAdjuster(mutableMapOf<String, Double>("north" to 1.09, "retail" to 0.89)),
            MinimumTieredBrokerScoreAdjuster(12.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 82590.6,
    )
}
