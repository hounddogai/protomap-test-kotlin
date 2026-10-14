package com.acme.transport.freightpay.accessorial.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the accessorial rule calculation. */
interface AccessorialRuleModifier {
    fun describe(): String
    fun adjust(item: AccessorialRule, value: Double): Double
}

data class FlaggedAccessorialRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AccessorialRuleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AccessorialRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingAccessorialRuleAdjuster(var step: Double) : AccessorialRuleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AccessorialRule, value: Double): Double = roundTo(value, step)
}

data class CategoryAccessorialRuleAdjuster(var factors: MutableMap<String, Double>) : AccessorialRuleModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: AccessorialRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAccessorialRuleAdjuster(var minimum: Double) : AccessorialRuleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AccessorialRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the accessorial rule steps in order and keeps a trace of each. */
data class AccessorialRuleEngine(
    var steps: MutableList<AccessorialRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AccessorialRule): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AccessorialRuleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AccessorialRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAccessorialRuleEngine(flags: Registry): AccessorialRuleEngine {
    return AccessorialRuleEngine(
        steps = mutableListOf<AccessorialRuleModifier>(
            FlaggedAccessorialRuleAdjuster(flags, "accessorial-rule-regional", 4.7),
            RoundingAccessorialRuleAdjuster(0.25),
            CategoryAccessorialRuleAdjuster(mutableMapOf<String, Double>("west" to 1.16, "east" to 1.25)),
            MinimumAccessorialRuleAdjuster(10.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 93997.6,
    )
}
