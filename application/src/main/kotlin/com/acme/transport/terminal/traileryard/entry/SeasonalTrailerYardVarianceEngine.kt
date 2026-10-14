package com.acme.transport.terminal.traileryard.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal trailer yard variance calculation. */
interface SeasonalTrailerYardVarianceModifier {
    fun describe(): String
    fun adjust(item: SeasonalTrailerYardVariance, value: Double): Double
}

data class CapSeasonalTrailerYardVarianceAdjuster(var cap: Double) : SeasonalTrailerYardVarianceModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalTrailerYardVariance, value: Double): Double = minOf(value, cap)
}

data class FlaggedSeasonalTrailerYardVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalTrailerYardVarianceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalTrailerYardVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingSeasonalTrailerYardVarianceAdjuster(var step: Double) : SeasonalTrailerYardVarianceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalTrailerYardVariance, value: Double): Double = roundTo(value, step)
}

data class PercentSeasonalTrailerYardVarianceAdjuster(
    var percent: Double,
) : SeasonalTrailerYardVarianceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalTrailerYardVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SeasonalTrailerYardVarianceEtaMinutesOffsetAdjuster(
    var weight: Double,
) : SeasonalTrailerYardVarianceModifier {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: SeasonalTrailerYardVariance, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

/** Runs the seasonal trailer yard variance steps in order and keeps a trace of each. */
data class SeasonalTrailerYardVarianceEngine(
    var steps: MutableList<SeasonalTrailerYardVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalTrailerYardVariance): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalTrailerYardVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalTrailerYardVarianceEngine(flags: Registry): SeasonalTrailerYardVarianceEngine {
    return SeasonalTrailerYardVarianceEngine(
        steps = mutableListOf<SeasonalTrailerYardVarianceModifier>(
            CapSeasonalTrailerYardVarianceAdjuster(25519.0),
            FlaggedSeasonalTrailerYardVarianceAdjuster(flags, "seasonal-trailer-yard-variance-strict", 5.7),
            RoundingSeasonalTrailerYardVarianceAdjuster(5.0),
            PercentSeasonalTrailerYardVarianceAdjuster(-17.9),
            SeasonalTrailerYardVarianceEtaMinutesOffsetAdjuster(0.87),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 45442.2,
    )
}
