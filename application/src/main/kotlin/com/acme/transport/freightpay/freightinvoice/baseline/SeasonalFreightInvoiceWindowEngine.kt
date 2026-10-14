package com.acme.transport.freightpay.freightinvoice.baseline

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal freight invoice window calculation. */
interface SeasonalFreightInvoiceWindowModifier {
    fun describe(): String
    fun adjust(item: SeasonalFreightInvoiceWindow, value: Double): Double
}

data class MinimumSeasonalFreightInvoiceWindowAdjuster(
    var minimum: Double,
) : SeasonalFreightInvoiceWindowModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalFreightInvoiceWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SeasonalFreightInvoiceWindowEndsAtOffsetAdjuster(
    var weight: Double,
) : SeasonalFreightInvoiceWindowModifier {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: SeasonalFreightInvoiceWindow, value: Double): Double {
        return value + item.endsAt.toDouble() * weight
    }
}

data class FlaggedSeasonalFreightInvoiceWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalFreightInvoiceWindowModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalFreightInvoiceWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentSeasonalFreightInvoiceWindowAdjuster(
    var percent: Double,
) : SeasonalFreightInvoiceWindowModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalFreightInvoiceWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the seasonal freight invoice window steps in order and keeps a trace of each. */
data class SeasonalFreightInvoiceWindowEngine(
    var steps: MutableList<SeasonalFreightInvoiceWindowModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalFreightInvoiceWindow): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalFreightInvoiceWindowModifier = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalFreightInvoiceWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalFreightInvoiceWindowEngine(flags: Registry): SeasonalFreightInvoiceWindowEngine {
    return SeasonalFreightInvoiceWindowEngine(
        steps = mutableListOf<SeasonalFreightInvoiceWindowModifier>(
            MinimumSeasonalFreightInvoiceWindowAdjuster(15.3),
            SeasonalFreightInvoiceWindowEndsAtOffsetAdjuster(0.9),
            FlaggedSeasonalFreightInvoiceWindowAdjuster(flags, "seasonal-freight-invoice-window-primary", -2.2),
            PercentSeasonalFreightInvoiceWindowAdjuster(-9.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 98243.1,
    )
}
