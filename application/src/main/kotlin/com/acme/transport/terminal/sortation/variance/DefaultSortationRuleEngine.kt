package com.acme.transport.terminal.sortation.variance

import com.acme.transport.kit.flags.Registry

/** One step of the default sortation rule calculation. */
interface DefaultSortationRuleModifier {
    fun describe(): String
    fun adjust(item: DefaultSortationRule, value: Double): Double
}

data class TieredDefaultSortationRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultSortationRuleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultSortationRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDefaultSortationRuleAdjuster(var minimum: Double) : DefaultSortationRuleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultSortationRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDefaultSortationRuleAdjuster(var percent: Double) : DefaultSortationRuleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultSortationRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedDefaultSortationRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultSortationRuleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultSortationRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DefaultSortationRuleEndsAtOffsetAdjuster(var weight: Double) : DefaultSortationRuleModifier {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: DefaultSortationRule, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class CapDefaultSortationRuleAdjuster(var cap: Double) : DefaultSortationRuleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultSortationRule, value: Double): Double = minOf(value, cap)
}

/** Runs the default sortation rule steps in order and keeps a trace of each. */
data class DefaultSortationRuleEngine(
    var steps: MutableList<DefaultSortationRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultSortationRule): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultSortationRuleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultSortationRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultSortationRuleEngine(flags: Registry): DefaultSortationRuleEngine {
    return DefaultSortationRuleEngine(
        steps = mutableListOf<DefaultSortationRuleModifier>(
            TieredDefaultSortationRuleAdjuster(
                mutableListOf<Double>(5979.0, 6887.0, 8503.0),
                mutableListOf<Double>(0.8, 0.85, 1.14),
            ),
            MinimumDefaultSortationRuleAdjuster(49.2),
            PercentDefaultSortationRuleAdjuster(12.6),
            FlaggedDefaultSortationRuleAdjuster(flags, "default-sortation-rule-global", -5.6),
            DefaultSortationRuleEndsAtOffsetAdjuster(1.8),
            CapDefaultSortationRuleAdjuster(16064.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 71092.6,
    )
}
