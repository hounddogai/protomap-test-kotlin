package com.acme.transport.customs.declaration.audit

import com.acme.transport.kit.flags.Registry

/** One step of the regional declaration audit calculation. */
interface RegionalDeclarationAuditStep {
    fun describe(): String
    fun adjust(item: RegionalDeclarationAudit, value: Double): Double
}

data class RegionalDeclarationAuditScoreOffsetAdjuster(var weight: Double) : RegionalDeclarationAuditStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: RegionalDeclarationAudit, value: Double): Double = value + item.score * weight
}

data class MinimumRegionalDeclarationAuditAdjuster(var minimum: Double) : RegionalDeclarationAuditStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalDeclarationAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRegionalDeclarationAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalDeclarationAuditStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalDeclarationAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelRegionalDeclarationAuditAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalDeclarationAuditStep {
    override fun describe(): String = "by label"

    override fun adjust(item: RegionalDeclarationAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRegionalDeclarationAuditAdjuster(var cap: Double) : RegionalDeclarationAuditStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalDeclarationAudit, value: Double): Double = minOf(value, cap)
}

data class FlaggedRegionalDeclarationAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalDeclarationAuditStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalDeclarationAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional declaration audit steps in order and keeps a trace of each. */
data class RegionalDeclarationAuditEngine(
    var steps: MutableList<RegionalDeclarationAuditStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalDeclarationAudit): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalDeclarationAuditStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalDeclarationAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalDeclarationAuditEngine(flags: Registry): RegionalDeclarationAuditEngine {
    return RegionalDeclarationAuditEngine(
        steps = mutableListOf<RegionalDeclarationAuditStep>(
            RegionalDeclarationAuditScoreOffsetAdjuster(0.66),
            MinimumRegionalDeclarationAuditAdjuster(25.8),
            TieredRegionalDeclarationAuditAdjuster(
                mutableListOf<Double>(512.0, 1081.0, 6278.0, 7994.0),
                mutableListOf<Double>(1.11, 0.81, 0.86, 0.95),
            ),
            LabelRegionalDeclarationAuditAdjuster(mutableMapOf<String, Double>("south" to 0.71, "east" to 1.37)),
            CapRegionalDeclarationAuditAdjuster(21675.0),
            FlaggedRegionalDeclarationAuditAdjuster(flags, "regional-declaration-audit-scheduled", 9.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 43576.4,
    )
}
