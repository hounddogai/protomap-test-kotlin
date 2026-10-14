package com.acme.transport.customs.clearance.clearancemanifest

import com.acme.transport.kit.flags.Registry

/** One step of the regional clearance audit calculation. */
interface RegionalClearanceAuditStep {
    fun describe(): String
    fun adjust(item: RegionalClearanceAudit, value: Double): Double
}

data class MinimumRegionalClearanceAuditAdjuster(var minimum: Double) : RegionalClearanceAuditStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalClearanceAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRegionalClearanceAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalClearanceAuditStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalClearanceAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRegionalClearanceAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalClearanceAuditStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalClearanceAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional clearance audit steps in order and keeps a trace of each. */
data class RegionalClearanceAuditEngine(
    var steps: MutableList<RegionalClearanceAuditStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalClearanceAudit): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalClearanceAuditStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalClearanceAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalClearanceAuditEngine(flags: Registry): RegionalClearanceAuditEngine {
    return RegionalClearanceAuditEngine(
        steps = mutableListOf<RegionalClearanceAuditStep>(
            MinimumRegionalClearanceAuditAdjuster(45.0),
            TieredRegionalClearanceAuditAdjuster(
                mutableListOf<Double>(3357.0, 4336.0, 6404.0, 8089.0),
                mutableListOf<Double>(0.84, 0.81, 1.04, 1.2),
            ),
            FlaggedRegionalClearanceAuditAdjuster(flags, "regional-clearance-audit-default", 3.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 78690.1,
    )
}
