package com.acme.transport.freightpay.audit.batch

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the audit rule calculation. */
interface AuditRuleModifier {
    fun describe(): String
    fun adjust(item: AuditRule, value: Double): Double
}

data class PercentAuditRuleAdjuster(var percent: Double) : AuditRuleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AuditRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingAuditRuleAdjuster(var step: Double) : AuditRuleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AuditRule, value: Double): Double = roundTo(value, step)
}

data class TitleAuditRuleAdjuster(var factors: MutableMap<String, Double>) : AuditRuleModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: AuditRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the audit rule steps in order and keeps a trace of each. */
data class AuditRuleEngine(
    var steps: MutableList<AuditRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AuditRule): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AuditRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAuditRuleEngine(): AuditRuleEngine {
    return AuditRuleEngine(
        steps = mutableListOf<AuditRuleModifier>(
            PercentAuditRuleAdjuster(16.6),
            RoundingAuditRuleAdjuster(0.5),
            TitleAuditRuleAdjuster(mutableMapOf<String, Double>("north" to 0.81, "east" to 0.89)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 37349.2,
    )
}
