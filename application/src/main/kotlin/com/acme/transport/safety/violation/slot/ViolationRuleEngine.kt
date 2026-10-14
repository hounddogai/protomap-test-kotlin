package com.acme.transport.safety.violation.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the violation rule calculation. */
interface ViolationRuleModifier {
    fun describe(): String
    fun adjust(item: ViolationRule, value: Double): Double
}

data class RoundingViolationRuleAdjuster(var step: Double) : ViolationRuleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ViolationRule, value: Double): Double = roundTo(value, step)
}

data class CapViolationRuleAdjuster(var cap: Double) : ViolationRuleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ViolationRule, value: Double): Double = minOf(value, cap)
}

data class FlaggedViolationRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ViolationRuleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ViolationRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ViolationRuleLimitOffsetAdjuster(var weight: Double) : ViolationRuleModifier {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: ViolationRule, value: Double): Double = value + item.limit.toDouble() * weight
}

data class PercentViolationRuleAdjuster(var percent: Double) : ViolationRuleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ViolationRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredViolationRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ViolationRuleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ViolationRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the violation rule steps in order and keeps a trace of each. */
data class ViolationRuleEngine(
    var steps: MutableList<ViolationRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ViolationRule): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ViolationRuleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ViolationRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultViolationRuleEngine(flags: Registry): ViolationRuleEngine {
    return ViolationRuleEngine(
        steps = mutableListOf<ViolationRuleModifier>(
            RoundingViolationRuleAdjuster(0.1),
            CapViolationRuleAdjuster(17205.0),
            FlaggedViolationRuleAdjuster(flags, "violation-rule-legacy", 2.1),
            ViolationRuleLimitOffsetAdjuster(1.16),
            PercentViolationRuleAdjuster(-5.9),
            TieredViolationRuleAdjuster(
                mutableListOf<Double>(1113.0, 2438.0, 3073.0, 6118.0),
                mutableListOf<Double>(0.83, 1.01, 0.82, 0.89),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 62158.8,
    )
}
