package com.acme.transport.freightpay.freightinvoice.overrideitem

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global freight invoice rule calculation. */
interface GlobalFreightInvoiceRuleModifier {
    fun describe(): String
    fun adjust(item: GlobalFreightInvoiceRule, value: Double): Double
}

data class SourceGlobalFreightInvoiceRuleAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalFreightInvoiceRuleModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: GlobalFreightInvoiceRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingGlobalFreightInvoiceRuleAdjuster(var step: Double) : GlobalFreightInvoiceRuleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalFreightInvoiceRule, value: Double): Double = roundTo(value, step)
}

data class PercentGlobalFreightInvoiceRuleAdjuster(var percent: Double) : GlobalFreightInvoiceRuleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalFreightInvoiceRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapGlobalFreightInvoiceRuleAdjuster(var cap: Double) : GlobalFreightInvoiceRuleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalFreightInvoiceRule, value: Double): Double = minOf(value, cap)
}

/** Runs the global freight invoice rule steps in order and keeps a trace of each. */
data class GlobalFreightInvoiceRuleEngine(
    var steps: MutableList<GlobalFreightInvoiceRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalFreightInvoiceRule): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalFreightInvoiceRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalFreightInvoiceRuleEngine(): GlobalFreightInvoiceRuleEngine {
    return GlobalFreightInvoiceRuleEngine(
        steps = mutableListOf<GlobalFreightInvoiceRuleModifier>(
            SourceGlobalFreightInvoiceRuleAdjuster(
                mutableMapOf<String, Double>("south" to 1.02, "retail" to 1.34),
            ),
            RoundingGlobalFreightInvoiceRuleAdjuster(0.5),
            PercentGlobalFreightInvoiceRuleAdjuster(-2.7),
            CapGlobalFreightInvoiceRuleAdjuster(30029.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 57541.6,
    )
}
