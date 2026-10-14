package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal accessorial score calculation. */
interface SeasonalAccessorialScoreFactor {
    fun describe(): String
    fun adjust(item: SeasonalAccessorialScore, value: Double): Double
}

data class LocaleSeasonalAccessorialScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalAccessorialScoreFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: SeasonalAccessorialScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredSeasonalAccessorialScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalAccessorialScoreFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalAccessorialScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentSeasonalAccessorialScoreAdjuster(var percent: Double) : SeasonalAccessorialScoreFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalAccessorialScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingSeasonalAccessorialScoreAdjuster(var step: Double) : SeasonalAccessorialScoreFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalAccessorialScore, value: Double): Double = roundTo(value, step)
}

data class FlaggedSeasonalAccessorialScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalAccessorialScoreFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalAccessorialScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumSeasonalAccessorialScoreAdjuster(var minimum: Double) : SeasonalAccessorialScoreFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalAccessorialScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the seasonal accessorial score steps in order and keeps a trace of each. */
data class SeasonalAccessorialScoreEngine(
    var steps: MutableList<SeasonalAccessorialScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalAccessorialScore): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalAccessorialScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalAccessorialScoreEngine(flags: Registry): SeasonalAccessorialScoreEngine {
    return SeasonalAccessorialScoreEngine(
        steps = mutableListOf<SeasonalAccessorialScoreFactor>(
            LocaleSeasonalAccessorialScoreAdjuster(
                mutableMapOf<String, Double>("north" to 1.38, "south" to 1.36, "east" to 1.38),
            ),
            TieredSeasonalAccessorialScoreAdjuster(
                mutableListOf<Double>(8901.0, 9710.0, 9765.0),
                mutableListOf<Double>(1.01, 0.85, 1.09),
            ),
            PercentSeasonalAccessorialScoreAdjuster(6.9),
            RoundingSeasonalAccessorialScoreAdjuster(0.25),
            FlaggedSeasonalAccessorialScoreAdjuster(flags, "seasonal-accessorial-score-rolling", -4.0),
            MinimumSeasonalAccessorialScoreAdjuster(16.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 99627.7,
    )
}
