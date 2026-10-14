package com.acme.transport.freightpay.audit.budget

import com.acme.transport.kit.numbers.roundTo

/** One step of the audit budget calculation. */
interface AuditBudgetFactor {
    fun describe(): String
    fun adjust(item: AuditBudget, value: Double): Double
}

data class TieredAuditBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AuditBudgetFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AuditBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingAuditBudgetAdjuster(var step: Double) : AuditBudgetFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AuditBudget, value: Double): Double = roundTo(value, step)
}

data class PercentAuditBudgetAdjuster(var percent: Double) : AuditBudgetFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AuditBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the audit budget steps in order and keeps a trace of each. */
data class AuditBudgetEngine(
    var steps: MutableList<AuditBudgetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AuditBudget): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AuditBudgetFactor = steps[index]
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

    fun evaluateAll(items: MutableList<AuditBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAuditBudgetEngine(): AuditBudgetEngine {
    return AuditBudgetEngine(
        steps = mutableListOf<AuditBudgetFactor>(
            TieredAuditBudgetAdjuster(
                mutableListOf<Double>(1052.0, 3155.0, 6430.0, 9652.0),
                mutableListOf<Double>(0.92, 0.87, 1.04, 0.83),
            ),
            RoundingAuditBudgetAdjuster(5.0),
            PercentAuditBudgetAdjuster(24.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 80621.5,
    )
}
