package com.acme.transport.freightpay.freightinvoice.schedule

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback freight invoice checkpoint calculation. */
interface FallbackFreightInvoiceCheckpointStep {
    fun describe(): String
    fun adjust(item: FallbackFreightInvoiceCheckpoint, value: Double): Double
}

data class CapFallbackFreightInvoiceCheckpointAdjuster(var cap: Double) : FallbackFreightInvoiceCheckpointStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackFreightInvoiceCheckpoint, value: Double): Double = minOf(value, cap)
}

data class FallbackFreightInvoiceCheckpointCreatedAtOffsetAdjuster(
    var weight: Double,
) : FallbackFreightInvoiceCheckpointStep {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: FallbackFreightInvoiceCheckpoint, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

data class SourceFallbackFreightInvoiceCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackFreightInvoiceCheckpointStep {
    override fun describe(): String = "by source"

    override fun adjust(item: FallbackFreightInvoiceCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredFallbackFreightInvoiceCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackFreightInvoiceCheckpointStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackFreightInvoiceCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fallback freight invoice checkpoint steps in order and keeps a trace of each. */
data class FallbackFreightInvoiceCheckpointEngine(
    var steps: MutableList<FallbackFreightInvoiceCheckpointStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackFreightInvoiceCheckpoint): Double {
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

    fun evaluateAll(items: MutableList<FallbackFreightInvoiceCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackFreightInvoiceCheckpointEngine(): FallbackFreightInvoiceCheckpointEngine {
    return FallbackFreightInvoiceCheckpointEngine(
        steps = mutableListOf<FallbackFreightInvoiceCheckpointStep>(
            CapFallbackFreightInvoiceCheckpointAdjuster(41911.0),
            FallbackFreightInvoiceCheckpointCreatedAtOffsetAdjuster(1.37),
            SourceFallbackFreightInvoiceCheckpointAdjuster(
                mutableMapOf<String, Double>("online" to 1.25, "north" to 1.31, "east" to 1.38),
            ),
            TieredFallbackFreightInvoiceCheckpointAdjuster(
                mutableListOf<Double>(3394.0, 4934.0, 7127.0),
                mutableListOf<Double>(0.83, 1.18, 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 1436.6,
    )
}
