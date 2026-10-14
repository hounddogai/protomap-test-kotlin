package com.acme.transport.freightpay.freightinvoice.rule

import com.acme.transport.kit.numbers.roundTo

/** One step of the freight invoice rule calculation. */
interface FreightInvoiceRuleAdjuster {
    fun describe(): String
    fun adjust(item: FreightInvoiceRule, value: Double): Double
}

data class NameFreightInvoiceRuleAdjuster(
    var factors: MutableMap<String, Double>,
) : FreightInvoiceRuleAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: FreightInvoiceRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapFreightInvoiceRuleAdjuster(var cap: Double) : FreightInvoiceRuleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FreightInvoiceRule, value: Double): Double = minOf(value, cap)
}

data class PercentFreightInvoiceRuleAdjuster(var percent: Double) : FreightInvoiceRuleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FreightInvoiceRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingFreightInvoiceRuleAdjuster(var step: Double) : FreightInvoiceRuleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FreightInvoiceRule, value: Double): Double = roundTo(value, step)
}

/** Runs the freight invoice rule steps in order and keeps a trace of each. */
data class FreightInvoiceRuleEngine(
    var steps: MutableList<FreightInvoiceRuleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FreightInvoiceRule): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FreightInvoiceRuleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FreightInvoiceRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFreightInvoiceRuleEngine(): FreightInvoiceRuleEngine {
    return FreightInvoiceRuleEngine(
        steps = mutableListOf<FreightInvoiceRuleAdjuster>(
            NameFreightInvoiceRuleAdjuster(mutableMapOf<String, Double>("retail" to 1.3)),
            CapFreightInvoiceRuleAdjuster(1763.0),
            PercentFreightInvoiceRuleAdjuster(20.2),
            RoundingFreightInvoiceRuleAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 78800.8,
    )
}
