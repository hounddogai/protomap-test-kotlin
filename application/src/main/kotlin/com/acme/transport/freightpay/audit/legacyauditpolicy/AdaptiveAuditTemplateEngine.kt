package com.acme.transport.freightpay.audit.legacyauditpolicy

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive audit template calculation. */
interface AdaptiveAuditTemplateStep {
    fun describe(): String
    fun adjust(item: AdaptiveAuditTemplate, value: Double): Double
}

data class AdaptiveAuditTemplateWindowDaysOffsetAdjuster(var weight: Double) : AdaptiveAuditTemplateStep {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: AdaptiveAuditTemplate, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class RoundingAdaptiveAuditTemplateAdjuster(var step: Double) : AdaptiveAuditTemplateStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveAuditTemplate, value: Double): Double = roundTo(value, step)
}

data class PercentAdaptiveAuditTemplateAdjuster(var percent: Double) : AdaptiveAuditTemplateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveAuditTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the adaptive audit template steps in order and keeps a trace of each. */
data class AdaptiveAuditTemplateEngine(
    var steps: MutableList<AdaptiveAuditTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveAuditTemplate): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveAuditTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveAuditTemplateEngine(): AdaptiveAuditTemplateEngine {
    return AdaptiveAuditTemplateEngine(
        steps = mutableListOf<AdaptiveAuditTemplateStep>(
            AdaptiveAuditTemplateWindowDaysOffsetAdjuster(1.69),
            RoundingAdaptiveAuditTemplateAdjuster(5.0),
            PercentAdaptiveAuditTemplateAdjuster(-1.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 93929.7,
    )
}
