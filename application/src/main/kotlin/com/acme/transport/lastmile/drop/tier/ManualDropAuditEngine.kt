package com.acme.transport.lastmile.drop.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual drop audit calculation. */
interface ManualDropAuditStep {
    fun describe(): String
    fun adjust(item: ManualDropAudit, value: Double): Double
}

data class ManualDropAuditLoadKgOffsetAdjuster(var weight: Double) : ManualDropAuditStep {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: ManualDropAudit, value: Double): Double = value + item.loadKg * weight
}

data class RoundingManualDropAuditAdjuster(var step: Double) : ManualDropAuditStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualDropAudit, value: Double): Double = roundTo(value, step)
}

data class MinimumManualDropAuditAdjuster(var minimum: Double) : ManualDropAuditStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualDropAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedManualDropAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualDropAuditStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualDropAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the manual drop audit steps in order and keeps a trace of each. */
data class ManualDropAuditEngine(
    var steps: MutableList<ManualDropAuditStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualDropAudit): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualDropAuditStep = steps[index]
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

    fun evaluateAll(items: MutableList<ManualDropAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualDropAuditEngine(flags: Registry): ManualDropAuditEngine {
    return ManualDropAuditEngine(
        steps = mutableListOf<ManualDropAuditStep>(
            ManualDropAuditLoadKgOffsetAdjuster(0.51),
            RoundingManualDropAuditAdjuster(5.0),
            MinimumManualDropAuditAdjuster(3.3),
            FlaggedManualDropAuditAdjuster(flags, "manual-drop-audit-tiered", -7.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 94614.8,
    )
}
