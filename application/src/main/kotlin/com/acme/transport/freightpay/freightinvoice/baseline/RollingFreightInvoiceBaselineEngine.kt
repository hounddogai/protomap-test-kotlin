package com.acme.transport.freightpay.freightinvoice.baseline

/** One step of the rolling freight invoice baseline calculation. */
interface RollingFreightInvoiceBaselineStep {
    fun describe(): String
    fun adjust(item: RollingFreightInvoiceBaseline, value: Double): Double
}

data class CapRollingFreightInvoiceBaselineAdjuster(var cap: Double) : RollingFreightInvoiceBaselineStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingFreightInvoiceBaseline, value: Double): Double = minOf(value, cap)
}

data class SourceRollingFreightInvoiceBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingFreightInvoiceBaselineStep {
    override fun describe(): String = "by source"

    override fun adjust(item: RollingFreightInvoiceBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRollingFreightInvoiceBaselineAdjuster(
    var minimum: Double,
) : RollingFreightInvoiceBaselineStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingFreightInvoiceBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rolling freight invoice baseline steps in order and keeps a trace of each. */
data class RollingFreightInvoiceBaselineEngine(
    var steps: MutableList<RollingFreightInvoiceBaselineStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingFreightInvoiceBaseline): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingFreightInvoiceBaselineStep = steps[index]
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

    fun evaluateAll(items: MutableList<RollingFreightInvoiceBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingFreightInvoiceBaselineEngine(): RollingFreightInvoiceBaselineEngine {
    return RollingFreightInvoiceBaselineEngine(
        steps = mutableListOf<RollingFreightInvoiceBaselineStep>(
            CapRollingFreightInvoiceBaselineAdjuster(49819.0),
            SourceRollingFreightInvoiceBaselineAdjuster(
                mutableMapOf<String, Double>("west" to 1.27, "east" to 0.73, "central" to 0.88),
            ),
            MinimumRollingFreightInvoiceBaselineAdjuster(39.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 40532.8,
    )
}
