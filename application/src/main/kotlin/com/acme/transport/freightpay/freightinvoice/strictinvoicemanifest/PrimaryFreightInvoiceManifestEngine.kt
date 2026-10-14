package com.acme.transport.freightpay.freightinvoice.strictinvoicemanifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary freight invoice manifest calculation. */
interface PrimaryFreightInvoiceManifestFactor {
    fun describe(): String
    fun adjust(item: PrimaryFreightInvoiceManifest, value: Double): Double
}

data class RoundingPrimaryFreightInvoiceManifestAdjuster(
    var step: Double,
) : PrimaryFreightInvoiceManifestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryFreightInvoiceManifest, value: Double): Double = roundTo(value, step)
}

data class FlaggedPrimaryFreightInvoiceManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryFreightInvoiceManifestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryFreightInvoiceManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPrimaryFreightInvoiceManifestAdjuster(
    var minimum: Double,
) : PrimaryFreightInvoiceManifestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryFreightInvoiceManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentPrimaryFreightInvoiceManifestAdjuster(
    var percent: Double,
) : PrimaryFreightInvoiceManifestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryFreightInvoiceManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredPrimaryFreightInvoiceManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryFreightInvoiceManifestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryFreightInvoiceManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NamePrimaryFreightInvoiceManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryFreightInvoiceManifestFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: PrimaryFreightInvoiceManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary freight invoice manifest steps in order and keeps a trace of each. */
data class PrimaryFreightInvoiceManifestEngine(
    var steps: MutableList<PrimaryFreightInvoiceManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryFreightInvoiceManifest): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryFreightInvoiceManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryFreightInvoiceManifestEngine(flags: Registry): PrimaryFreightInvoiceManifestEngine {
    return PrimaryFreightInvoiceManifestEngine(
        steps = mutableListOf<PrimaryFreightInvoiceManifestFactor>(
            RoundingPrimaryFreightInvoiceManifestAdjuster(5.0),
            FlaggedPrimaryFreightInvoiceManifestAdjuster(flags, "primary-freight-invoice-manifest-global", -9.8),
            MinimumPrimaryFreightInvoiceManifestAdjuster(12.1),
            PercentPrimaryFreightInvoiceManifestAdjuster(10.5),
            TieredPrimaryFreightInvoiceManifestAdjuster(
                mutableListOf<Double>(2712.0, 4147.0, 4472.0, 7990.0),
                mutableListOf<Double>(1.04, 1.05, 1.16, 1.18),
            ),
            NamePrimaryFreightInvoiceManifestAdjuster(
                mutableMapOf<String, Double>("online" to 1.16, "east" to 0.78, "retail" to 0.79),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 73970.9,
    )
}
