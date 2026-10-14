package com.acme.transport.freightpay.audit.auditrule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive audit rule calculation. */
interface AdaptiveAuditRuleFactor {
    fun describe(): String
    fun adjust(item: AdaptiveAuditRule, value: Double): Double
}

data class TieredAdaptiveAuditRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveAuditRuleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveAuditRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedAdaptiveAuditRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveAuditRuleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveAuditRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapAdaptiveAuditRuleAdjuster(var cap: Double) : AdaptiveAuditRuleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveAuditRule, value: Double): Double = minOf(value, cap)
}

data class RoundingAdaptiveAuditRuleAdjuster(var step: Double) : AdaptiveAuditRuleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveAuditRule, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive audit rule steps in order and keeps a trace of each. */
data class AdaptiveAuditRuleEngine(
    var steps: MutableList<AdaptiveAuditRuleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveAuditRule): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveAuditRuleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveAuditRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveAuditRuleEngine(flags: Registry): AdaptiveAuditRuleEngine {
    return AdaptiveAuditRuleEngine(
        steps = mutableListOf<AdaptiveAuditRuleFactor>(
            TieredAdaptiveAuditRuleAdjuster(
                mutableListOf<Double>(3143.0, 3205.0, 5666.0, 6186.0),
                mutableListOf<Double>(0.83, 0.89, 1.04, 0.95),
            ),
            FlaggedAdaptiveAuditRuleAdjuster(flags, "adaptive-audit-rule-strict", -7.1),
            CapAdaptiveAuditRuleAdjuster(21769.0),
            RoundingAdaptiveAuditRuleAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 78997.0,
    )
}
