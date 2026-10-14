package com.acme.transport.network.hub.hubdraft

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled hub rule calculation. */
interface ScheduledHubRuleFactor {
    fun describe(): String
    fun adjust(item: ScheduledHubRule, value: Double): Double
}

data class ScheduledHubRuleAmountOffsetAdjuster(var weight: Double) : ScheduledHubRuleFactor {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: ScheduledHubRule, value: Double): Double = value + item.amount * weight
}

data class TieredScheduledHubRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledHubRuleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledHubRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapScheduledHubRuleAdjuster(var cap: Double) : ScheduledHubRuleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledHubRule, value: Double): Double = minOf(value, cap)
}

data class PercentScheduledHubRuleAdjuster(var percent: Double) : ScheduledHubRuleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledHubRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameScheduledHubRuleAdjuster(var factors: MutableMap<String, Double>) : ScheduledHubRuleFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: ScheduledHubRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedScheduledHubRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledHubRuleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledHubRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the scheduled hub rule steps in order and keeps a trace of each. */
data class ScheduledHubRuleEngine(
    var steps: MutableList<ScheduledHubRuleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledHubRule): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledHubRuleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledHubRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledHubRuleEngine(flags: Registry): ScheduledHubRuleEngine {
    return ScheduledHubRuleEngine(
        steps = mutableListOf<ScheduledHubRuleFactor>(
            ScheduledHubRuleAmountOffsetAdjuster(1.68),
            TieredScheduledHubRuleAdjuster(
                mutableListOf<Double>(1158.0, 3597.0),
                mutableListOf<Double>(0.85, 0.81),
            ),
            CapScheduledHubRuleAdjuster(10447.0),
            PercentScheduledHubRuleAdjuster(9.2),
            NameScheduledHubRuleAdjuster(
                mutableMapOf<String, Double>("retail" to 0.95, "central" to 0.77, "east" to 0.93),
            ),
            FlaggedScheduledHubRuleAdjuster(flags, "scheduled-hub-rule-seasonal", 7.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 46679.5,
    )
}
