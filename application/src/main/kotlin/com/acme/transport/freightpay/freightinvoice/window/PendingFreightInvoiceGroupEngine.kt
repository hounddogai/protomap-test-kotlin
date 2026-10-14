package com.acme.transport.freightpay.freightinvoice.window

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending freight invoice group calculation. */
interface PendingFreightInvoiceGroupFactor {
    fun describe(): String
    fun adjust(item: PendingFreightInvoiceGroup, value: Double): Double
}

data class PercentPendingFreightInvoiceGroupAdjuster(var percent: Double) : PendingFreightInvoiceGroupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingFreightInvoiceGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class PendingFreightInvoiceGroupAttemptsOffsetAdjuster(
    var weight: Double,
) : PendingFreightInvoiceGroupFactor {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: PendingFreightInvoiceGroup, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

data class RegionPendingFreightInvoiceGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingFreightInvoiceGroupFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: PendingFreightInvoiceGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredPendingFreightInvoiceGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingFreightInvoiceGroupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingFreightInvoiceGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumPendingFreightInvoiceGroupAdjuster(var minimum: Double) : PendingFreightInvoiceGroupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingFreightInvoiceGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the pending freight invoice group steps in order and keeps a trace of each. */
data class PendingFreightInvoiceGroupEngine(
    var steps: MutableList<PendingFreightInvoiceGroupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingFreightInvoiceGroup): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingFreightInvoiceGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingFreightInvoiceGroupEngine(): PendingFreightInvoiceGroupEngine {
    return PendingFreightInvoiceGroupEngine(
        steps = mutableListOf<PendingFreightInvoiceGroupFactor>(
            PercentPendingFreightInvoiceGroupAdjuster(6.2),
            PendingFreightInvoiceGroupAttemptsOffsetAdjuster(0.46),
            RegionPendingFreightInvoiceGroupAdjuster(mutableMapOf<String, Double>("central" to 1.07)),
            TieredPendingFreightInvoiceGroupAdjuster(
                mutableListOf<Double>(8446.0, 9357.0),
                mutableListOf<Double>(1.05, 1.0),
            ),
            MinimumPendingFreightInvoiceGroupAdjuster(13.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 86571.0,
    )
}
