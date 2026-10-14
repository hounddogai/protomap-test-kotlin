package com.acme.transport.freightpay.freightinvoice.invoicemanifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the freight invoice plan calculation. */
interface FreightInvoicePlanModifier {
    fun describe(): String
    fun adjust(item: FreightInvoicePlan, value: Double): Double
}

data class FlaggedFreightInvoicePlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FreightInvoicePlanModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FreightInvoicePlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingFreightInvoicePlanAdjuster(var step: Double) : FreightInvoicePlanModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FreightInvoicePlan, value: Double): Double = roundTo(value, step)
}

data class FreightInvoicePlanGraceDaysOffsetAdjuster(var weight: Double) : FreightInvoicePlanModifier {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: FreightInvoicePlan, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class MinimumFreightInvoicePlanAdjuster(var minimum: Double) : FreightInvoicePlanModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FreightInvoicePlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelFreightInvoicePlanAdjuster(
    var factors: MutableMap<String, Double>,
) : FreightInvoicePlanModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: FreightInvoicePlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the freight invoice plan steps in order and keeps a trace of each. */
data class FreightInvoicePlanEngine(
    var steps: MutableList<FreightInvoicePlanModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FreightInvoicePlan): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FreightInvoicePlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFreightInvoicePlanEngine(flags: Registry): FreightInvoicePlanEngine {
    return FreightInvoicePlanEngine(
        steps = mutableListOf<FreightInvoicePlanModifier>(
            FlaggedFreightInvoicePlanAdjuster(flags, "freight-invoice-plan-tiered", -1.7),
            RoundingFreightInvoicePlanAdjuster(0.1),
            FreightInvoicePlanGraceDaysOffsetAdjuster(1.65),
            MinimumFreightInvoicePlanAdjuster(26.0),
            LabelFreightInvoicePlanAdjuster(mutableMapOf<String, Double>("retail" to 1.38)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 70126.8,
    )
}
