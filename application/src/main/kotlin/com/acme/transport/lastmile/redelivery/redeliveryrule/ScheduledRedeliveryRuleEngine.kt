package com.acme.transport.lastmile.redelivery.redeliveryrule

import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled redelivery rule calculation. */
interface ScheduledRedeliveryRuleFactor {
    fun describe(): String
    fun adjust(item: ScheduledRedeliveryRule, value: Double): Double
}

data class MinimumScheduledRedeliveryRuleAdjuster(var minimum: Double) : ScheduledRedeliveryRuleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledRedeliveryRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredScheduledRedeliveryRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledRedeliveryRuleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledRedeliveryRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingScheduledRedeliveryRuleAdjuster(var step: Double) : ScheduledRedeliveryRuleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledRedeliveryRule, value: Double): Double = roundTo(value, step)
}

data class ScheduledRedeliveryRuleRevisionOffsetAdjuster(var weight: Double) : ScheduledRedeliveryRuleFactor {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: ScheduledRedeliveryRule, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

/** Runs the scheduled redelivery rule steps in order and keeps a trace of each. */
data class ScheduledRedeliveryRuleEngine(
    var steps: MutableList<ScheduledRedeliveryRuleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledRedeliveryRule): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledRedeliveryRuleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledRedeliveryRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledRedeliveryRuleEngine(): ScheduledRedeliveryRuleEngine {
    return ScheduledRedeliveryRuleEngine(
        steps = mutableListOf<ScheduledRedeliveryRuleFactor>(
            MinimumScheduledRedeliveryRuleAdjuster(28.5),
            TieredScheduledRedeliveryRuleAdjuster(
                mutableListOf<Double>(479.0, 4598.0, 7229.0, 8871.0),
                mutableListOf<Double>(0.81, 0.86, 0.9, 1.14),
            ),
            RoundingScheduledRedeliveryRuleAdjuster(0.1),
            ScheduledRedeliveryRuleRevisionOffsetAdjuster(1.37),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 95434.7,
    )
}
