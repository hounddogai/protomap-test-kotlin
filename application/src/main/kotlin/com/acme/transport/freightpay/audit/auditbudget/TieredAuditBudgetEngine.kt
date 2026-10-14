package com.acme.transport.freightpay.audit.auditbudget

import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered audit budget calculation. */
interface TieredAuditBudgetModifier {
    fun describe(): String
    fun adjust(item: TieredAuditBudget, value: Double): Double
}

data class CountryTieredAuditBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredAuditBudgetModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: TieredAuditBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingTieredAuditBudgetAdjuster(var step: Double) : TieredAuditBudgetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredAuditBudget, value: Double): Double = roundTo(value, step)
}

data class CapTieredAuditBudgetAdjuster(var cap: Double) : TieredAuditBudgetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredAuditBudget, value: Double): Double = minOf(value, cap)
}

/** Runs the tiered audit budget steps in order and keeps a trace of each. */
data class TieredAuditBudgetEngine(
    var steps: MutableList<TieredAuditBudgetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredAuditBudget): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredAuditBudgetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TieredAuditBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredAuditBudgetEngine(): TieredAuditBudgetEngine {
    return TieredAuditBudgetEngine(
        steps = mutableListOf<TieredAuditBudgetModifier>(
            CountryTieredAuditBudgetAdjuster(mutableMapOf<String, Double>("online" to 1.38)),
            RoundingTieredAuditBudgetAdjuster(0.1),
            CapTieredAuditBudgetAdjuster(28472.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 61883.7,
    )
}
