package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.kit.flags.Registry

/** One step of the attempt rule calculation. */
interface AttemptRuleStep {
    fun describe(): String
    fun adjust(item: AttemptRule, value: Double): Double
}

data class MinimumAttemptRuleAdjuster(var minimum: Double) : AttemptRuleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AttemptRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class AttemptRuleDiscountOffsetAdjuster(var weight: Double) : AttemptRuleStep {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: AttemptRule, value: Double): Double = value + item.discount * weight
}

data class FlaggedAttemptRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AttemptRuleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AttemptRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelAttemptRuleAdjuster(var factors: MutableMap<String, Double>) : AttemptRuleStep {
    override fun describe(): String = "by label"

    override fun adjust(item: AttemptRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentAttemptRuleAdjuster(var percent: Double) : AttemptRuleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AttemptRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAttemptRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AttemptRuleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AttemptRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the attempt rule steps in order and keeps a trace of each. */
data class AttemptRuleEngine(
    var steps: MutableList<AttemptRuleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AttemptRule): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AttemptRuleStep = steps[index]
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

    fun evaluateAll(items: MutableList<AttemptRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAttemptRuleEngine(flags: Registry): AttemptRuleEngine {
    return AttemptRuleEngine(
        steps = mutableListOf<AttemptRuleStep>(
            MinimumAttemptRuleAdjuster(18.3),
            AttemptRuleDiscountOffsetAdjuster(1.54),
            FlaggedAttemptRuleAdjuster(flags, "attempt-rule-scheduled", -1.5),
            LabelAttemptRuleAdjuster(
                mutableMapOf<String, Double>("online" to 0.8, "retail" to 1.31, "north" to 0.94),
            ),
            PercentAttemptRuleAdjuster(12.1),
            TieredAttemptRuleAdjuster(
                mutableListOf<Double>(4071.0, 5628.0, 8421.0),
                mutableListOf<Double>(0.83, 1.08, 0.8),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 15013.8,
    )
}
