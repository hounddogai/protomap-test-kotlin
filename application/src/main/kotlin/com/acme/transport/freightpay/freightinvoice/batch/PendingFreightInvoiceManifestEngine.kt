package com.acme.transport.freightpay.freightinvoice.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending freight invoice manifest calculation. */
interface PendingFreightInvoiceManifestFactor {
    fun describe(): String
    fun adjust(item: PendingFreightInvoiceManifest, value: Double): Double
}

data class MinimumPendingFreightInvoiceManifestAdjuster(
    var minimum: Double,
) : PendingFreightInvoiceManifestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingFreightInvoiceManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedPendingFreightInvoiceManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingFreightInvoiceManifestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingFreightInvoiceManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelPendingFreightInvoiceManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingFreightInvoiceManifestFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: PendingFreightInvoiceManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredPendingFreightInvoiceManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingFreightInvoiceManifestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingFreightInvoiceManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the pending freight invoice manifest steps in order and keeps a trace of each. */
data class PendingFreightInvoiceManifestEngine(
    var steps: MutableList<PendingFreightInvoiceManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingFreightInvoiceManifest): Double {
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

    fun evaluateAll(items: MutableList<PendingFreightInvoiceManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingFreightInvoiceManifestEngine(flags: Registry): PendingFreightInvoiceManifestEngine {
    return PendingFreightInvoiceManifestEngine(
        steps = mutableListOf<PendingFreightInvoiceManifestFactor>(
            MinimumPendingFreightInvoiceManifestAdjuster(27.0),
            FlaggedPendingFreightInvoiceManifestAdjuster(
                flags,
                "pending-freight-invoice-manifest-fallback",
                -2.1,
            ),
            LabelPendingFreightInvoiceManifestAdjuster(
                mutableMapOf<String, Double>("east" to 0.98, "retail" to 1.38),
            ),
            TieredPendingFreightInvoiceManifestAdjuster(
                mutableListOf<Double>(2692.0, 6913.0),
                mutableListOf<Double>(1.11, 1.07),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 68756.4,
    )
}
