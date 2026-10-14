package com.acme.transport.emissions.evcharge.assignment

import com.acme.transport.kit.flags.Registry

/** One step of the ev charge rule calculation. */
interface EvChargeRuleModifier {
    fun describe(): String
    fun adjust(item: EvChargeRule, value: Double): Double
}

data class PercentEvChargeRuleAdjuster(var percent: Double) : EvChargeRuleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EvChargeRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class EvChargeRuleAmountOffsetAdjuster(var weight: Double) : EvChargeRuleModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: EvChargeRule, value: Double): Double = value + item.amount * weight
}

data class MinimumEvChargeRuleAdjuster(var minimum: Double) : EvChargeRuleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EvChargeRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedEvChargeRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EvChargeRuleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EvChargeRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the ev charge rule steps in order and keeps a trace of each. */
data class EvChargeRuleEngine(
    var steps: MutableList<EvChargeRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: EvChargeRule): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EvChargeRuleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<EvChargeRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultEvChargeRuleEngine(flags: Registry): EvChargeRuleEngine {
    return EvChargeRuleEngine(
        steps = mutableListOf<EvChargeRuleModifier>(
            PercentEvChargeRuleAdjuster(15.9),
            EvChargeRuleAmountOffsetAdjuster(0.96),
            MinimumEvChargeRuleAdjuster(25.9),
            FlaggedEvChargeRuleAdjuster(flags, "ev-charge-rule-global", -4.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 76705.9,
    )
}
