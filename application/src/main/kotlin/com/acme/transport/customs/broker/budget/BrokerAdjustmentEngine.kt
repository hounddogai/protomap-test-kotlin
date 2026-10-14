package com.acme.transport.customs.broker.budget

import com.acme.transport.kit.numbers.roundTo

/** One step of the broker adjustment calculation. */
interface BrokerAdjustmentAdjuster {
    fun describe(): String
    fun adjust(item: BrokerAdjustment, value: Double): Double
}

data class TitleBrokerAdjustmentAdjuster(var factors: MutableMap<String, Double>) : BrokerAdjustmentAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: BrokerAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingBrokerAdjustmentAdjuster(var step: Double) : BrokerAdjustmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BrokerAdjustment, value: Double): Double = roundTo(value, step)
}

data class TieredBrokerAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BrokerAdjustmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BrokerAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the broker adjustment steps in order and keeps a trace of each. */
data class BrokerAdjustmentEngine(
    var steps: MutableList<BrokerAdjustmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BrokerAdjustment): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BrokerAdjustmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BrokerAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBrokerAdjustmentEngine(): BrokerAdjustmentEngine {
    return BrokerAdjustmentEngine(
        steps = mutableListOf<BrokerAdjustmentAdjuster>(
            TitleBrokerAdjustmentAdjuster(mutableMapOf<String, Double>("retail" to 0.81, "north" to 1.16)),
            RoundingBrokerAdjustmentAdjuster(0.5),
            TieredBrokerAdjustmentAdjuster(
                mutableListOf<Double>(640.0, 2174.0, 8328.0),
                mutableListOf<Double>(1.04, 0.99, 0.98),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 84550.7,
    )
}
