package com.acme.transport.freightpay.freightinvoice.invoiceadjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary freight invoice adjustment calculation. */
interface PrimaryFreightInvoiceAdjustmentModifier {
    fun describe(): String
    fun adjust(item: PrimaryFreightInvoiceAdjustment, value: Double): Double
}

data class FlaggedPrimaryFreightInvoiceAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryFreightInvoiceAdjustmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryFreightInvoiceAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PrimaryFreightInvoiceAdjustmentStopCountOffsetAdjuster(
    var weight: Double,
) : PrimaryFreightInvoiceAdjustmentModifier {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: PrimaryFreightInvoiceAdjustment, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

data class PercentPrimaryFreightInvoiceAdjustmentAdjuster(
    var percent: Double,
) : PrimaryFreightInvoiceAdjustmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryFreightInvoiceAdjustment, value: Double): Double {
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelPrimaryFreightInvoiceAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryFreightInvoiceAdjustmentModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: PrimaryFreightInvoiceAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary freight invoice adjustment steps in order and keeps a trace of each. */
data class PrimaryFreightInvoiceAdjustmentEngine(
    var steps: MutableList<PrimaryFreightInvoiceAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryFreightInvoiceAdjustment): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryFreightInvoiceAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryFreightInvoiceAdjustmentEngine(flags: Registry): PrimaryFreightInvoiceAdjustmentEngine {
    return PrimaryFreightInvoiceAdjustmentEngine(
        steps = mutableListOf<PrimaryFreightInvoiceAdjustmentModifier>(
            FlaggedPrimaryFreightInvoiceAdjustmentAdjuster(
                flags,
                "primary-freight-invoice-adjustment-lenient",
                -9.0,
            ),
            PrimaryFreightInvoiceAdjustmentStopCountOffsetAdjuster(0.41),
            PercentPrimaryFreightInvoiceAdjustmentAdjuster(-24.1),
            LabelPrimaryFreightInvoiceAdjustmentAdjuster(
                mutableMapOf<String, Double>("retail" to 1.21, "central" to 1.04),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 21905.4,
    )
}
