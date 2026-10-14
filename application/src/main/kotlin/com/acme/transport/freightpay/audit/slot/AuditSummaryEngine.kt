package com.acme.transport.freightpay.audit.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the audit summary calculation. */
interface AuditSummaryStep {
    fun describe(): String
    fun adjust(item: AuditSummary, value: Double): Double
}

data class FlaggedAuditSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AuditSummaryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AuditSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameAuditSummaryAdjuster(var factors: MutableMap<String, Double>) : AuditSummaryStep {
    override fun describe(): String = "by name"

    override fun adjust(item: AuditSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAuditSummaryAdjuster(var minimum: Double) : AuditSummaryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AuditSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAuditSummaryAdjuster(var step: Double) : AuditSummaryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AuditSummary, value: Double): Double = roundTo(value, step)
}

data class PercentAuditSummaryAdjuster(var percent: Double) : AuditSummaryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AuditSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the audit summary steps in order and keeps a trace of each. */
data class AuditSummaryEngine(
    var steps: MutableList<AuditSummaryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AuditSummary): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AuditSummaryStep = steps[index]
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

    fun evaluateAll(items: MutableList<AuditSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAuditSummaryEngine(flags: Registry): AuditSummaryEngine {
    return AuditSummaryEngine(
        steps = mutableListOf<AuditSummaryStep>(
            FlaggedAuditSummaryAdjuster(flags, "audit-summary-global", -8.6),
            NameAuditSummaryAdjuster(mutableMapOf<String, Double>("east" to 1.13, "online" to 1.03)),
            MinimumAuditSummaryAdjuster(12.8),
            RoundingAuditSummaryAdjuster(1.0),
            PercentAuditSummaryAdjuster(21.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 98115.1,
    )
}
