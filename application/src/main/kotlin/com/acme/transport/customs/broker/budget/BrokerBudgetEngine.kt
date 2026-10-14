package com.acme.transport.customs.broker.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the broker budget calculation. */
interface BrokerBudgetModifier {
    fun describe(): String
    fun adjust(item: BrokerBudget, value: Double): Double
}

data class TitleBrokerBudgetAdjuster(var factors: MutableMap<String, Double>) : BrokerBudgetModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: BrokerBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentBrokerBudgetAdjuster(var percent: Double) : BrokerBudgetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BrokerBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedBrokerBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BrokerBudgetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BrokerBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredBrokerBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BrokerBudgetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BrokerBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumBrokerBudgetAdjuster(var minimum: Double) : BrokerBudgetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BrokerBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class BrokerBudgetSequenceOffsetAdjuster(var weight: Double) : BrokerBudgetModifier {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: BrokerBudget, value: Double): Double = value + item.sequence.toDouble() * weight
}

/** Runs the broker budget steps in order and keeps a trace of each. */
data class BrokerBudgetEngine(
    var steps: MutableList<BrokerBudgetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BrokerBudget): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BrokerBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBrokerBudgetEngine(flags: Registry): BrokerBudgetEngine {
    return BrokerBudgetEngine(
        steps = mutableListOf<BrokerBudgetModifier>(
            TitleBrokerBudgetAdjuster(
                mutableMapOf<String, Double>("west" to 1.14, "central" to 0.74, "south" to 0.72),
            ),
            PercentBrokerBudgetAdjuster(-1.5),
            FlaggedBrokerBudgetAdjuster(flags, "broker-budget-weighted", 4.4),
            TieredBrokerBudgetAdjuster(mutableListOf<Double>(6273.0, 7235.0), mutableListOf<Double>(0.97, 0.85)),
            MinimumBrokerBudgetAdjuster(43.2),
            BrokerBudgetSequenceOffsetAdjuster(1.87),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 17772.8,
    )
}
