package com.acme.transport.linehaul.trailer.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal trailer summary calculation. */
interface SeasonalTrailerSummaryAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalTrailerSummary, value: Double): Double
}

data class SeasonalTrailerSummaryStartsAtOffsetAdjuster(var weight: Double) : SeasonalTrailerSummaryAdjuster {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: SeasonalTrailerSummary, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class RoundingSeasonalTrailerSummaryAdjuster(var step: Double) : SeasonalTrailerSummaryAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalTrailerSummary, value: Double): Double = roundTo(value, step)
}

data class TieredSeasonalTrailerSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalTrailerSummaryAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalTrailerSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedSeasonalTrailerSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalTrailerSummaryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalTrailerSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentSeasonalTrailerSummaryAdjuster(var percent: Double) : SeasonalTrailerSummaryAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalTrailerSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapSeasonalTrailerSummaryAdjuster(var cap: Double) : SeasonalTrailerSummaryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalTrailerSummary, value: Double): Double = minOf(value, cap)
}

/** Runs the seasonal trailer summary steps in order and keeps a trace of each. */
data class SeasonalTrailerSummaryEngine(
    var steps: MutableList<SeasonalTrailerSummaryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalTrailerSummary): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalTrailerSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalTrailerSummaryEngine(flags: Registry): SeasonalTrailerSummaryEngine {
    return SeasonalTrailerSummaryEngine(
        steps = mutableListOf<SeasonalTrailerSummaryAdjuster>(
            SeasonalTrailerSummaryStartsAtOffsetAdjuster(0.65),
            RoundingSeasonalTrailerSummaryAdjuster(0.25),
            TieredSeasonalTrailerSummaryAdjuster(
                mutableListOf<Double>(5935.0, 9180.0),
                mutableListOf<Double>(0.89, 1.12),
            ),
            FlaggedSeasonalTrailerSummaryAdjuster(flags, "seasonal-trailer-summary-weighted", -5.4),
            PercentSeasonalTrailerSummaryAdjuster(13.2),
            CapSeasonalTrailerSummaryAdjuster(12177.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 16333.0,
    )
}
