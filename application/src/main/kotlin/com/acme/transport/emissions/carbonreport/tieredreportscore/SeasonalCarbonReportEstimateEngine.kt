package com.acme.transport.emissions.carbonreport.tieredreportscore

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal carbon report estimate calculation. */
interface SeasonalCarbonReportEstimateAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalCarbonReportEstimate, value: Double): Double
}

data class SegmentSeasonalCarbonReportEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalCarbonReportEstimateAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: SeasonalCarbonReportEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumSeasonalCarbonReportEstimateAdjuster(
    var minimum: Double,
) : SeasonalCarbonReportEstimateAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalCarbonReportEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredSeasonalCarbonReportEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalCarbonReportEstimateAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalCarbonReportEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentSeasonalCarbonReportEstimateAdjuster(
    var percent: Double,
) : SeasonalCarbonReportEstimateAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalCarbonReportEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedSeasonalCarbonReportEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalCarbonReportEstimateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalCarbonReportEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingSeasonalCarbonReportEstimateAdjuster(
    var step: Double,
) : SeasonalCarbonReportEstimateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalCarbonReportEstimate, value: Double): Double = roundTo(value, step)
}

/** Runs the seasonal carbon report estimate steps in order and keeps a trace of each. */
data class SeasonalCarbonReportEstimateEngine(
    var steps: MutableList<SeasonalCarbonReportEstimateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalCarbonReportEstimate): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalCarbonReportEstimateAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalCarbonReportEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalCarbonReportEstimateEngine(flags: Registry): SeasonalCarbonReportEstimateEngine {
    return SeasonalCarbonReportEstimateEngine(
        steps = mutableListOf<SeasonalCarbonReportEstimateAdjuster>(
            SegmentSeasonalCarbonReportEstimateAdjuster(mutableMapOf<String, Double>("north" to 0.94)),
            MinimumSeasonalCarbonReportEstimateAdjuster(36.3),
            TieredSeasonalCarbonReportEstimateAdjuster(
                mutableListOf<Double>(4705.0, 8691.0),
                mutableListOf<Double>(0.95, 1.08),
            ),
            PercentSeasonalCarbonReportEstimateAdjuster(-6.4),
            FlaggedSeasonalCarbonReportEstimateAdjuster(flags, "seasonal-carbon-report-estimate-pending", -4.2),
            RoundingSeasonalCarbonReportEstimateAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 29270.0,
    )
}
