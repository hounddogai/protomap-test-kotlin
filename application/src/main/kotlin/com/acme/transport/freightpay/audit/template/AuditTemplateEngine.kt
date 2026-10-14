package com.acme.transport.freightpay.audit.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the audit template calculation. */
interface AuditTemplateStep {
    fun describe(): String
    fun adjust(item: AuditTemplate, value: Double): Double
}

data class PercentAuditTemplateAdjuster(var percent: Double) : AuditTemplateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AuditTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedAuditTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AuditTemplateStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AuditTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumAuditTemplateAdjuster(var minimum: Double) : AuditTemplateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AuditTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the audit template steps in order and keeps a trace of each. */
data class AuditTemplateEngine(
    var steps: MutableList<AuditTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AuditTemplate): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AuditTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAuditTemplateEngine(flags: Registry): AuditTemplateEngine {
    return AuditTemplateEngine(
        steps = mutableListOf<AuditTemplateStep>(
            PercentAuditTemplateAdjuster(-0.5),
            FlaggedAuditTemplateAdjuster(flags, "audit-template-capped", 0.3),
            MinimumAuditTemplateAdjuster(36.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 75310.0,
    )
}
