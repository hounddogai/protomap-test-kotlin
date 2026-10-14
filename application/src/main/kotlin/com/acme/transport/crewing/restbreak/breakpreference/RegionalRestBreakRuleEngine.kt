package com.acme.transport.crewing.restbreak.breakpreference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional rest break rule calculation. */
interface RegionalRestBreakRuleModifier {
    fun describe(): String
    fun adjust(item: RegionalRestBreakRule, value: Double): Double
}

data class MinimumRegionalRestBreakRuleAdjuster(var minimum: Double) : RegionalRestBreakRuleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalRestBreakRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRegionalRestBreakRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalRestBreakRuleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalRestBreakRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRegionalRestBreakRuleAdjuster(var cap: Double) : RegionalRestBreakRuleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalRestBreakRule, value: Double): Double = minOf(value, cap)
}

data class RoundingRegionalRestBreakRuleAdjuster(var step: Double) : RegionalRestBreakRuleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalRestBreakRule, value: Double): Double = roundTo(value, step)
}

/** Runs the regional rest break rule steps in order and keeps a trace of each. */
data class RegionalRestBreakRuleEngine(
    var steps: MutableList<RegionalRestBreakRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalRestBreakRule): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalRestBreakRuleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalRestBreakRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalRestBreakRuleEngine(flags: Registry): RegionalRestBreakRuleEngine {
    return RegionalRestBreakRuleEngine(
        steps = mutableListOf<RegionalRestBreakRuleModifier>(
            MinimumRegionalRestBreakRuleAdjuster(8.5),
            FlaggedRegionalRestBreakRuleAdjuster(flags, "regional-rest-break-rule-lenient", 9.2),
            CapRegionalRestBreakRuleAdjuster(24257.0),
            RoundingRegionalRestBreakRuleAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 55448.1,
    )
}
