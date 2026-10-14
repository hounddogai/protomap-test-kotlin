package com.acme.transport.linehaul.backhaul.backhaulquota

import com.acme.transport.kit.flags.Registry

/** One step of the manual backhaul audit calculation. */
interface ManualBackhaulAuditStep {
    fun describe(): String
    fun adjust(item: ManualBackhaulAudit, value: Double): Double
}

data class ManualBackhaulAuditRevisionOffsetAdjuster(var weight: Double) : ManualBackhaulAuditStep {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: ManualBackhaulAudit, value: Double): Double = value + item.revision.toDouble() * weight
}

data class TieredManualBackhaulAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualBackhaulAuditStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualBackhaulAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedManualBackhaulAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualBackhaulAuditStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualBackhaulAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapManualBackhaulAuditAdjuster(var cap: Double) : ManualBackhaulAuditStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualBackhaulAudit, value: Double): Double = minOf(value, cap)
}

/** Runs the manual backhaul audit steps in order and keeps a trace of each. */
data class ManualBackhaulAuditEngine(
    var steps: MutableList<ManualBackhaulAuditStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualBackhaulAudit): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualBackhaulAuditStep = steps[index]
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

    fun evaluateAll(items: MutableList<ManualBackhaulAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualBackhaulAuditEngine(flags: Registry): ManualBackhaulAuditEngine {
    return ManualBackhaulAuditEngine(
        steps = mutableListOf<ManualBackhaulAuditStep>(
            ManualBackhaulAuditRevisionOffsetAdjuster(0.74),
            TieredManualBackhaulAuditAdjuster(
                mutableListOf<Double>(4701.0, 9086.0),
                mutableListOf<Double>(0.93, 0.89),
            ),
            FlaggedManualBackhaulAuditAdjuster(flags, "manual-backhaul-audit-strict", 5.3),
            CapManualBackhaulAuditAdjuster(43704.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 68534.4,
    )
}
