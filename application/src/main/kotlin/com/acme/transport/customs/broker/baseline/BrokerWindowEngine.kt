package com.acme.transport.customs.broker.baseline

import com.acme.transport.kit.numbers.roundTo

/** One step of the broker window calculation. */
interface BrokerWindowAdjuster {
    fun describe(): String
    fun adjust(item: BrokerWindow, value: Double): Double
}

data class LabelBrokerWindowAdjuster(var factors: MutableMap<String, Double>) : BrokerWindowAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: BrokerWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredBrokerWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BrokerWindowAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BrokerWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingBrokerWindowAdjuster(var step: Double) : BrokerWindowAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BrokerWindow, value: Double): Double = roundTo(value, step)
}

/** Runs the broker window steps in order and keeps a trace of each. */
data class BrokerWindowEngine(
    var steps: MutableList<BrokerWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BrokerWindow): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BrokerWindowAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BrokerWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBrokerWindowEngine(): BrokerWindowEngine {
    return BrokerWindowEngine(
        steps = mutableListOf<BrokerWindowAdjuster>(
            LabelBrokerWindowAdjuster(mutableMapOf<String, Double>("east" to 1.23)),
            TieredBrokerWindowAdjuster(
                mutableListOf<Double>(536.0, 5451.0, 5477.0, 6191.0),
                mutableListOf<Double>(1.11, 0.81, 0.98, 0.81),
            ),
            RoundingBrokerWindowAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 63596.2,
    )
}
