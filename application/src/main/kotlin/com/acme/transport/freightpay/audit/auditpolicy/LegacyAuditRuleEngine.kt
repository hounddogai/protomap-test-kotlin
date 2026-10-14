package com.acme.transport.freightpay.audit.auditpolicy

import com.acme.transport.kit.flags.Registry

/** One step of the legacy audit rule calculation. */
interface LegacyAuditRuleStep {
    fun describe(): String
    fun adjust(item: LegacyAuditRule, value: Double): Double
}

data class MinimumLegacyAuditRuleAdjuster(var minimum: Double) : LegacyAuditRuleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyAuditRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLegacyAuditRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyAuditRuleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyAuditRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLegacyAuditRuleAdjuster(var percent: Double) : LegacyAuditRuleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyAuditRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameLegacyAuditRuleAdjuster(var factors: MutableMap<String, Double>) : LegacyAuditRuleStep {
    override fun describe(): String = "by name"

    override fun adjust(item: LegacyAuditRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class LegacyAuditRuleLoadKgOffsetAdjuster(var weight: Double) : LegacyAuditRuleStep {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: LegacyAuditRule, value: Double): Double = value + item.loadKg * weight
}

data class TieredLegacyAuditRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyAuditRuleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyAuditRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the legacy audit rule steps in order and keeps a trace of each. */
data class LegacyAuditRuleEngine(
    var steps: MutableList<LegacyAuditRuleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyAuditRule): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyAuditRuleStep = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyAuditRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyAuditRuleEngine(flags: Registry): LegacyAuditRuleEngine {
    return LegacyAuditRuleEngine(
        steps = mutableListOf<LegacyAuditRuleStep>(
            MinimumLegacyAuditRuleAdjuster(8.2),
            FlaggedLegacyAuditRuleAdjuster(flags, "legacy-audit-rule-weighted", 0.7),
            PercentLegacyAuditRuleAdjuster(17.4),
            NameLegacyAuditRuleAdjuster(mutableMapOf<String, Double>("online" to 1.39)),
            LegacyAuditRuleLoadKgOffsetAdjuster(0.76),
            TieredLegacyAuditRuleAdjuster(
                mutableListOf<Double>(822.0, 872.0, 1188.0),
                mutableListOf<Double>(0.9, 1.15, 0.97),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 12978.5,
    )
}
