package com.acme.transport.freightpay.freightinvoice.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched freight invoice override calculation. */
interface BatchedFreightInvoiceOverrideFactor {
    fun describe(): String
    fun adjust(item: BatchedFreightInvoiceOverride, value: Double): Double
}

data class CapBatchedFreightInvoiceOverrideAdjuster(var cap: Double) : BatchedFreightInvoiceOverrideFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedFreightInvoiceOverride, value: Double): Double = minOf(value, cap)
}

data class LabelBatchedFreightInvoiceOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedFreightInvoiceOverrideFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: BatchedFreightInvoiceOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedBatchedFreightInvoiceOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedFreightInvoiceOverrideFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedFreightInvoiceOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumBatchedFreightInvoiceOverrideAdjuster(
    var minimum: Double,
) : BatchedFreightInvoiceOverrideFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedFreightInvoiceOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the batched freight invoice override steps in order and keeps a trace of each. */
data class BatchedFreightInvoiceOverrideEngine(
    var steps: MutableList<BatchedFreightInvoiceOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedFreightInvoiceOverride): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedFreightInvoiceOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedFreightInvoiceOverrideEngine(flags: Registry): BatchedFreightInvoiceOverrideEngine {
    return BatchedFreightInvoiceOverrideEngine(
        steps = mutableListOf<BatchedFreightInvoiceOverrideFactor>(
            CapBatchedFreightInvoiceOverrideAdjuster(36000.0),
            LabelBatchedFreightInvoiceOverrideAdjuster(
                mutableMapOf<String, Double>("retail" to 1.04, "east" to 1.34, "north" to 0.75),
            ),
            FlaggedBatchedFreightInvoiceOverrideAdjuster(flags, "batched-freight-invoice-override-capped", -1.6),
            MinimumBatchedFreightInvoiceOverrideAdjuster(11.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 77660.6,
    )
}
