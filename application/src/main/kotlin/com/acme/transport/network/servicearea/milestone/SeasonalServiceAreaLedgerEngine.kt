package com.acme.transport.network.servicearea.milestone

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal service area ledger calculation. */
interface SeasonalServiceAreaLedgerAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalServiceAreaLedger, value: Double): Double
}

data class TieredSeasonalServiceAreaLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalServiceAreaLedgerAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalServiceAreaLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentSeasonalServiceAreaLedgerAdjuster(var percent: Double) : SeasonalServiceAreaLedgerAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalServiceAreaLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedSeasonalServiceAreaLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalServiceAreaLedgerAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalServiceAreaLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LocaleSeasonalServiceAreaLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalServiceAreaLedgerAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: SeasonalServiceAreaLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumSeasonalServiceAreaLedgerAdjuster(var minimum: Double) : SeasonalServiceAreaLedgerAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalServiceAreaLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the seasonal service area ledger steps in order and keeps a trace of each. */
data class SeasonalServiceAreaLedgerEngine(
    var steps: MutableList<SeasonalServiceAreaLedgerAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalServiceAreaLedger): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalServiceAreaLedgerAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalServiceAreaLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalServiceAreaLedgerEngine(flags: Registry): SeasonalServiceAreaLedgerEngine {
    return SeasonalServiceAreaLedgerEngine(
        steps = mutableListOf<SeasonalServiceAreaLedgerAdjuster>(
            TieredSeasonalServiceAreaLedgerAdjuster(
                mutableListOf<Double>(3879.0, 6361.0),
                mutableListOf<Double>(0.85, 1.05),
            ),
            PercentSeasonalServiceAreaLedgerAdjuster(-13.2),
            FlaggedSeasonalServiceAreaLedgerAdjuster(flags, "seasonal-service-area-ledger-pending", -0.3),
            LocaleSeasonalServiceAreaLedgerAdjuster(mutableMapOf<String, Double>("central" to 0.73)),
            MinimumSeasonalServiceAreaLedgerAdjuster(44.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 95166.8,
    )
}
