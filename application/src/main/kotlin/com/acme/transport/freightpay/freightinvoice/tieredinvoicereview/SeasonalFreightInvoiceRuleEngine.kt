package com.acme.transport.freightpay.freightinvoice.tieredinvoicereview

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal freight invoice rule calculation. */
interface SeasonalFreightInvoiceRuleModifier {
    fun describe(): String
    fun adjust(item: SeasonalFreightInvoiceRule, value: Double): Double
}

data class RoundingSeasonalFreightInvoiceRuleAdjuster(var step: Double) : SeasonalFreightInvoiceRuleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalFreightInvoiceRule, value: Double): Double = roundTo(value, step)
}

data class FlaggedSeasonalFreightInvoiceRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalFreightInvoiceRuleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalFreightInvoiceRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SeasonalFreightInvoiceRuleCostOffsetAdjuster(
    var weight: Double,
) : SeasonalFreightInvoiceRuleModifier {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: SeasonalFreightInvoiceRule, value: Double): Double = value + item.cost * weight
}

data class CapSeasonalFreightInvoiceRuleAdjuster(var cap: Double) : SeasonalFreightInvoiceRuleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalFreightInvoiceRule, value: Double): Double = minOf(value, cap)
}

data class TieredSeasonalFreightInvoiceRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalFreightInvoiceRuleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalFreightInvoiceRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the seasonal freight invoice rule steps in order and keeps a trace of each. */
data class SeasonalFreightInvoiceRuleEngine(
    var steps: MutableList<SeasonalFreightInvoiceRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalFreightInvoiceRule): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalFreightInvoiceRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalFreightInvoiceRuleEngine(flags: Registry): SeasonalFreightInvoiceRuleEngine {
    return SeasonalFreightInvoiceRuleEngine(
        steps = mutableListOf<SeasonalFreightInvoiceRuleModifier>(
            RoundingSeasonalFreightInvoiceRuleAdjuster(0.05),
            FlaggedSeasonalFreightInvoiceRuleAdjuster(flags, "seasonal-freight-invoice-rule-global", -7.5),
            SeasonalFreightInvoiceRuleCostOffsetAdjuster(0.99),
            CapSeasonalFreightInvoiceRuleAdjuster(36058.0),
            TieredSeasonalFreightInvoiceRuleAdjuster(
                mutableListOf<Double>(2499.0, 3791.0, 8625.0, 9367.0),
                mutableListOf<Double>(1.02, 1.16, 1.05, 0.87),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 41282.0,
    )
}
