package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal freight invoice manifest calculation. */
interface SeasonalFreightInvoiceManifestModifier {
    fun describe(): String
    fun adjust(item: SeasonalFreightInvoiceManifest, value: Double): Double
}

data class FlaggedSeasonalFreightInvoiceManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalFreightInvoiceManifestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalFreightInvoiceManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredSeasonalFreightInvoiceManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalFreightInvoiceManifestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalFreightInvoiceManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumSeasonalFreightInvoiceManifestAdjuster(
    var minimum: Double,
) : SeasonalFreightInvoiceManifestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalFreightInvoiceManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the seasonal freight invoice manifest steps in order and keeps a trace of each. */
data class SeasonalFreightInvoiceManifestEngine(
    var steps: MutableList<SeasonalFreightInvoiceManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalFreightInvoiceManifest): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalFreightInvoiceManifestModifier = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalFreightInvoiceManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalFreightInvoiceManifestEngine(flags: Registry): SeasonalFreightInvoiceManifestEngine {
    return SeasonalFreightInvoiceManifestEngine(
        steps = mutableListOf<SeasonalFreightInvoiceManifestModifier>(
            FlaggedSeasonalFreightInvoiceManifestAdjuster(
                flags,
                "seasonal-freight-invoice-manifest-scheduled",
                4.3,
            ),
            TieredSeasonalFreightInvoiceManifestAdjuster(
                mutableListOf<Double>(833.0, 4636.0, 5853.0, 6201.0),
                mutableListOf<Double>(1.17, 1.01, 0.98, 1.05),
            ),
            MinimumSeasonalFreightInvoiceManifestAdjuster(40.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 5572.1,
    )
}
