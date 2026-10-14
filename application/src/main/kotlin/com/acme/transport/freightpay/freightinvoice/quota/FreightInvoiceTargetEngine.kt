package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the freight invoice target calculation. */
interface FreightInvoiceTargetStep {
    fun describe(): String
    fun adjust(item: FreightInvoiceTarget, value: Double): Double
}

data class TieredFreightInvoiceTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FreightInvoiceTargetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FreightInvoiceTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapFreightInvoiceTargetAdjuster(var cap: Double) : FreightInvoiceTargetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FreightInvoiceTarget, value: Double): Double = minOf(value, cap)
}

data class PercentFreightInvoiceTargetAdjuster(var percent: Double) : FreightInvoiceTargetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FreightInvoiceTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedFreightInvoiceTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FreightInvoiceTargetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FreightInvoiceTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleFreightInvoiceTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : FreightInvoiceTargetStep {
    override fun describe(): String = "by title"

    override fun adjust(item: FreightInvoiceTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the freight invoice target steps in order and keeps a trace of each. */
data class FreightInvoiceTargetEngine(
    var steps: MutableList<FreightInvoiceTargetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FreightInvoiceTarget): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FreightInvoiceTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFreightInvoiceTargetEngine(flags: Registry): FreightInvoiceTargetEngine {
    return FreightInvoiceTargetEngine(
        steps = mutableListOf<FreightInvoiceTargetStep>(
            TieredFreightInvoiceTargetAdjuster(
                mutableListOf<Double>(3316.0, 4149.0),
                mutableListOf<Double>(1.18, 1.09),
            ),
            CapFreightInvoiceTargetAdjuster(10895.0),
            PercentFreightInvoiceTargetAdjuster(16.2),
            FlaggedFreightInvoiceTargetAdjuster(flags, "freight-invoice-target-strict", -1.4),
            TitleFreightInvoiceTargetAdjuster(mutableMapOf<String, Double>("online" to 1.29, "south" to 0.95)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 89146.4,
    )
}
