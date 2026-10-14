package com.acme.transport.freightpay.freightinvoice.preference

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the freight invoice batch calculation. */
interface FreightInvoiceBatchAdjuster {
    fun describe(): String
    fun adjust(item: FreightInvoiceBatch, value: Double): Double
}

data class CapFreightInvoiceBatchAdjuster(var cap: Double) : FreightInvoiceBatchAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FreightInvoiceBatch, value: Double): Double = minOf(value, cap)
}

data class FreightInvoiceBatchDurationMinutesOffsetAdjuster(var weight: Double) : FreightInvoiceBatchAdjuster {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: FreightInvoiceBatch, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class MinimumFreightInvoiceBatchAdjuster(var minimum: Double) : FreightInvoiceBatchAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FreightInvoiceBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingFreightInvoiceBatchAdjuster(var step: Double) : FreightInvoiceBatchAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FreightInvoiceBatch, value: Double): Double = roundTo(value, step)
}

/** Runs the freight invoice batch steps in order and keeps a trace of each. */
data class FreightInvoiceBatchEngine(
    var steps: MutableList<FreightInvoiceBatchAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FreightInvoiceBatch): Double {
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

    fun evaluateAll(items: MutableList<FreightInvoiceBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFreightInvoiceBatchEngine(): FreightInvoiceBatchEngine {
    return FreightInvoiceBatchEngine(
        steps = mutableListOf<FreightInvoiceBatchAdjuster>(
            CapFreightInvoiceBatchAdjuster(23996.0),
            FreightInvoiceBatchDurationMinutesOffsetAdjuster(1.64),
            MinimumFreightInvoiceBatchAdjuster(24.2),
            RoundingFreightInvoiceBatchAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 78349.4,
    )
}
