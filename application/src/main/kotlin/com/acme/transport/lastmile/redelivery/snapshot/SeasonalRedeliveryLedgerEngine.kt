package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal redelivery ledger calculation. */
interface SeasonalRedeliveryLedgerStep {
    fun describe(): String
    fun adjust(item: SeasonalRedeliveryLedger, value: Double): Double
}

data class MinimumSeasonalRedeliveryLedgerAdjuster(var minimum: Double) : SeasonalRedeliveryLedgerStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalRedeliveryLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SeasonalRedeliveryLedgerPalletsOffsetAdjuster(var weight: Double) : SeasonalRedeliveryLedgerStep {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: SeasonalRedeliveryLedger, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class FlaggedSeasonalRedeliveryLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalRedeliveryLedgerStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalRedeliveryLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapSeasonalRedeliveryLedgerAdjuster(var cap: Double) : SeasonalRedeliveryLedgerStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalRedeliveryLedger, value: Double): Double = minOf(value, cap)
}

data class LabelSeasonalRedeliveryLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalRedeliveryLedgerStep {
    override fun describe(): String = "by label"

    override fun adjust(item: SeasonalRedeliveryLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentSeasonalRedeliveryLedgerAdjuster(var percent: Double) : SeasonalRedeliveryLedgerStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalRedeliveryLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the seasonal redelivery ledger steps in order and keeps a trace of each. */
data class SeasonalRedeliveryLedgerEngine(
    var steps: MutableList<SeasonalRedeliveryLedgerStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalRedeliveryLedger): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalRedeliveryLedgerStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalRedeliveryLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalRedeliveryLedgerEngine(flags: Registry): SeasonalRedeliveryLedgerEngine {
    return SeasonalRedeliveryLedgerEngine(
        steps = mutableListOf<SeasonalRedeliveryLedgerStep>(
            MinimumSeasonalRedeliveryLedgerAdjuster(14.4),
            SeasonalRedeliveryLedgerPalletsOffsetAdjuster(1.64),
            FlaggedSeasonalRedeliveryLedgerAdjuster(flags, "seasonal-redelivery-ledger-regional", -8.0),
            CapSeasonalRedeliveryLedgerAdjuster(18595.0),
            LabelSeasonalRedeliveryLedgerAdjuster(
                mutableMapOf<String, Double>("online" to 0.77, "central" to 1.33),
            ),
            PercentSeasonalRedeliveryLedgerAdjuster(24.7),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 59794.2,
    )
}
