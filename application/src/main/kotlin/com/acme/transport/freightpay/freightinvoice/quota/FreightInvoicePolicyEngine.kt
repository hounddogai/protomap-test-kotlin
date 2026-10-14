package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.kit.flags.Registry

/** One step of the freight invoice policy calculation. */
interface FreightInvoicePolicyStep {
    fun describe(): String
    fun adjust(item: FreightInvoicePolicy, value: Double): Double
}

data class FlaggedFreightInvoicePolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FreightInvoicePolicyStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FreightInvoicePolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumFreightInvoicePolicyAdjuster(var minimum: Double) : FreightInvoicePolicyStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FreightInvoicePolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelFreightInvoicePolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : FreightInvoicePolicyStep {
    override fun describe(): String = "by label"

    override fun adjust(item: FreightInvoicePolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the freight invoice policy steps in order and keeps a trace of each. */
data class FreightInvoicePolicyEngine(
    var steps: MutableList<FreightInvoicePolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FreightInvoicePolicy): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FreightInvoicePolicyStep = steps[index]
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

    fun evaluateAll(items: MutableList<FreightInvoicePolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFreightInvoicePolicyEngine(flags: Registry): FreightInvoicePolicyEngine {
    return FreightInvoicePolicyEngine(
        steps = mutableListOf<FreightInvoicePolicyStep>(
            FlaggedFreightInvoicePolicyAdjuster(flags, "freight-invoice-policy-default", 0.3),
            MinimumFreightInvoicePolicyAdjuster(4.3),
            LabelFreightInvoicePolicyAdjuster(mutableMapOf<String, Double>("north" to 1.38, "west" to 0.7)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 53398.3,
    )
}
