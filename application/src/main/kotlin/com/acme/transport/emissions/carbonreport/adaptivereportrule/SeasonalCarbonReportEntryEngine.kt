package com.acme.transport.emissions.carbonreport.adaptivereportrule

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal carbon report entry calculation. */
interface SeasonalCarbonReportEntryAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalCarbonReportEntry, value: Double): Double
}

data class DepotSeasonalCarbonReportEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalCarbonReportEntryAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: SeasonalCarbonReportEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class SeasonalCarbonReportEntryMarginOffsetAdjuster(
    var weight: Double,
) : SeasonalCarbonReportEntryAdjuster {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: SeasonalCarbonReportEntry, value: Double): Double = value + item.margin * weight
}

data class PercentSeasonalCarbonReportEntryAdjuster(var percent: Double) : SeasonalCarbonReportEntryAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalCarbonReportEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapSeasonalCarbonReportEntryAdjuster(var cap: Double) : SeasonalCarbonReportEntryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalCarbonReportEntry, value: Double): Double = minOf(value, cap)
}

data class TieredSeasonalCarbonReportEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalCarbonReportEntryAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalCarbonReportEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedSeasonalCarbonReportEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalCarbonReportEntryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalCarbonReportEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the seasonal carbon report entry steps in order and keeps a trace of each. */
data class SeasonalCarbonReportEntryEngine(
    var steps: MutableList<SeasonalCarbonReportEntryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalCarbonReportEntry): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalCarbonReportEntryAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalCarbonReportEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalCarbonReportEntryEngine(flags: Registry): SeasonalCarbonReportEntryEngine {
    return SeasonalCarbonReportEntryEngine(
        steps = mutableListOf<SeasonalCarbonReportEntryAdjuster>(
            DepotSeasonalCarbonReportEntryAdjuster(
                mutableMapOf<String, Double>("east" to 0.75, "north" to 0.97),
            ),
            SeasonalCarbonReportEntryMarginOffsetAdjuster(1.36),
            PercentSeasonalCarbonReportEntryAdjuster(13.5),
            CapSeasonalCarbonReportEntryAdjuster(21575.0),
            TieredSeasonalCarbonReportEntryAdjuster(
                mutableListOf<Double>(381.0, 1293.0, 8004.0, 9173.0),
                mutableListOf<Double>(1.16, 0.85, 0.84, 0.91),
            ),
            FlaggedSeasonalCarbonReportEntryAdjuster(flags, "seasonal-carbon-report-entry-scheduled", -5.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 73624.5,
    )
}
