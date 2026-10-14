package com.acme.transport.freightpay.freightinvoice.strictinvoicemanifest

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending freight invoice rule calculation. */
interface PendingFreightInvoiceRuleFactor {
    fun describe(): String
    fun adjust(item: PendingFreightInvoiceRule, value: Double): Double
}

data class ChannelPendingFreightInvoiceRuleAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingFreightInvoiceRuleFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: PendingFreightInvoiceRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPendingFreightInvoiceRuleAdjuster(var percent: Double) : PendingFreightInvoiceRuleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingFreightInvoiceRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class PendingFreightInvoiceRuleRatioOffsetAdjuster(var weight: Double) : PendingFreightInvoiceRuleFactor {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: PendingFreightInvoiceRule, value: Double): Double = value + item.ratio * weight
}

data class CapPendingFreightInvoiceRuleAdjuster(var cap: Double) : PendingFreightInvoiceRuleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingFreightInvoiceRule, value: Double): Double = minOf(value, cap)
}

/** Runs the pending freight invoice rule steps in order and keeps a trace of each. */
data class PendingFreightInvoiceRuleEngine(
    var steps: MutableList<PendingFreightInvoiceRuleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingFreightInvoiceRule): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingFreightInvoiceRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingFreightInvoiceRuleEngine(): PendingFreightInvoiceRuleEngine {
    return PendingFreightInvoiceRuleEngine(
        steps = mutableListOf<PendingFreightInvoiceRuleFactor>(
            ChannelPendingFreightInvoiceRuleAdjuster(
                mutableMapOf<String, Double>("west" to 1.35, "north" to 0.7, "south" to 0.99),
            ),
            PercentPendingFreightInvoiceRuleAdjuster(-18.1),
            PendingFreightInvoiceRuleRatioOffsetAdjuster(0.5),
            CapPendingFreightInvoiceRuleAdjuster(44729.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 68712.7,
    )
}
