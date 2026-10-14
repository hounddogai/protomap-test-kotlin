package com.acme.transport.customs.broker.forecast

import com.acme.transport.kit.numbers.roundTo

/** One step of the broker rule calculation. */
interface BrokerRuleAdjuster {
    fun describe(): String
    fun adjust(item: BrokerRule, value: Double): Double
}

data class CapBrokerRuleAdjuster(var cap: Double) : BrokerRuleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BrokerRule, value: Double): Double = minOf(value, cap)
}

data class PercentBrokerRuleAdjuster(var percent: Double) : BrokerRuleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BrokerRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredBrokerRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BrokerRuleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BrokerRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingBrokerRuleAdjuster(var step: Double) : BrokerRuleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BrokerRule, value: Double): Double = roundTo(value, step)
}

/** Runs the broker rule steps in order and keeps a trace of each. */
data class BrokerRuleEngine(
    var steps: MutableList<BrokerRuleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BrokerRule): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BrokerRuleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BrokerRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBrokerRuleEngine(): BrokerRuleEngine {
    return BrokerRuleEngine(
        steps = mutableListOf<BrokerRuleAdjuster>(
            CapBrokerRuleAdjuster(8793.0),
            PercentBrokerRuleAdjuster(-23.4),
            TieredBrokerRuleAdjuster(mutableListOf<Double>(1452.0, 6671.0), mutableListOf<Double>(0.93, 0.81)),
            RoundingBrokerRuleAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 76493.0,
    )
}
