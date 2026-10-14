package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the audit metric calculation. */
interface AuditMetricAdjuster {
    fun describe(): String
    fun adjust(item: AuditMetric, value: Double): Double
}

data class RoundingAuditMetricAdjuster(var step: Double) : AuditMetricAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AuditMetric, value: Double): Double = roundTo(value, step)
}

data class MinimumAuditMetricAdjuster(var minimum: Double) : AuditMetricAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AuditMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAuditMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AuditMetricAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AuditMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentAuditMetricAdjuster(var percent: Double) : AuditMetricAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AuditMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the audit metric steps in order and keeps a trace of each. */
data class AuditMetricEngine(
    var steps: MutableList<AuditMetricAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AuditMetric): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AuditMetricAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AuditMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAuditMetricEngine(flags: Registry): AuditMetricEngine {
    return AuditMetricEngine(
        steps = mutableListOf<AuditMetricAdjuster>(
            RoundingAuditMetricAdjuster(0.1),
            MinimumAuditMetricAdjuster(18.7),
            FlaggedAuditMetricAdjuster(flags, "audit-metric-scheduled", -7.0),
            PercentAuditMetricAdjuster(-1.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 27447.8,
    )
}
