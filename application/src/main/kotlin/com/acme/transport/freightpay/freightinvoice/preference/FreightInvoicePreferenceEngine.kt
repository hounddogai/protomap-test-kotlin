package com.acme.transport.freightpay.freightinvoice.preference

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the freight invoice preference calculation. */
interface FreightInvoicePreferenceStep {
    fun describe(): String
    fun adjust(item: FreightInvoicePreference, value: Double): Double
}

data class CapFreightInvoicePreferenceAdjuster(var cap: Double) : FreightInvoicePreferenceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FreightInvoicePreference, value: Double): Double = minOf(value, cap)
}

data class LabelFreightInvoicePreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : FreightInvoicePreferenceStep {
    override fun describe(): String = "by label"

    override fun adjust(item: FreightInvoicePreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentFreightInvoicePreferenceAdjuster(var percent: Double) : FreightInvoicePreferenceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FreightInvoicePreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FreightInvoicePreferenceMinimumOffsetAdjuster(var weight: Double) : FreightInvoicePreferenceStep {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: FreightInvoicePreference, value: Double): Double = value + item.minimum * weight
}

data class RoundingFreightInvoicePreferenceAdjuster(var step: Double) : FreightInvoicePreferenceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FreightInvoicePreference, value: Double): Double = roundTo(value, step)
}

/** Runs the freight invoice preference steps in order and keeps a trace of each. */
data class FreightInvoicePreferenceEngine(
    var steps: MutableList<FreightInvoicePreferenceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FreightInvoicePreference): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FreightInvoicePreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFreightInvoicePreferenceEngine(): FreightInvoicePreferenceEngine {
    return FreightInvoicePreferenceEngine(
        steps = mutableListOf<FreightInvoicePreferenceStep>(
            CapFreightInvoicePreferenceAdjuster(3299.0),
            LabelFreightInvoicePreferenceAdjuster(mutableMapOf<String, Double>("north" to 0.9)),
            PercentFreightInvoicePreferenceAdjuster(-21.6),
            FreightInvoicePreferenceMinimumOffsetAdjuster(0.96),
            RoundingFreightInvoicePreferenceAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 39638.0,
    )
}
