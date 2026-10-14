package com.acme.transport.freightpay.audit.overrideitem

import com.acme.transport.kit.numbers.roundTo

/** One step of the audit baseline calculation. */
interface AuditBaselineStep {
    fun describe(): String
    fun adjust(item: AuditBaseline, value: Double): Double
}

data class CapAuditBaselineAdjuster(var cap: Double) : AuditBaselineStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AuditBaseline, value: Double): Double = minOf(value, cap)
}

data class LabelAuditBaselineAdjuster(var factors: MutableMap<String, Double>) : AuditBaselineStep {
    override fun describe(): String = "by label"

    override fun adjust(item: AuditBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingAuditBaselineAdjuster(var step: Double) : AuditBaselineStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AuditBaseline, value: Double): Double = roundTo(value, step)
}

/** Runs the audit baseline steps in order and keeps a trace of each. */
data class AuditBaselineEngine(
    var steps: MutableList<AuditBaselineStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AuditBaseline): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AuditBaselineStep = steps[index]
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

    fun evaluateAll(items: MutableList<AuditBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAuditBaselineEngine(): AuditBaselineEngine {
    return AuditBaselineEngine(
        steps = mutableListOf<AuditBaselineStep>(
            CapAuditBaselineAdjuster(26520.0),
            LabelAuditBaselineAdjuster(
                mutableMapOf<String, Double>("central" to 1.03, "north" to 0.95, "east" to 0.77),
            ),
            RoundingAuditBaselineAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 56545.8,
    )
}
