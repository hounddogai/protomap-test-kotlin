package com.acme.transport.terminal.sortation.sortationvariance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal sortation variance calculation. */
interface SeasonalSortationVarianceAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalSortationVariance, value: Double): Double
}

data class RoundingSeasonalSortationVarianceAdjuster(var step: Double) : SeasonalSortationVarianceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalSortationVariance, value: Double): Double = roundTo(value, step)
}

data class SeasonalSortationVarianceCeilingOffsetAdjuster(
    var weight: Double,
) : SeasonalSortationVarianceAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: SeasonalSortationVariance, value: Double): Double = value + item.ceiling * weight
}

data class FlaggedSeasonalSortationVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalSortationVarianceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalSortationVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapSeasonalSortationVarianceAdjuster(var cap: Double) : SeasonalSortationVarianceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalSortationVariance, value: Double): Double = minOf(value, cap)
}

data class MinimumSeasonalSortationVarianceAdjuster(var minimum: Double) : SeasonalSortationVarianceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalSortationVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the seasonal sortation variance steps in order and keeps a trace of each. */
data class SeasonalSortationVarianceEngine(
    var steps: MutableList<SeasonalSortationVarianceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalSortationVariance): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalSortationVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalSortationVarianceEngine(flags: Registry): SeasonalSortationVarianceEngine {
    return SeasonalSortationVarianceEngine(
        steps = mutableListOf<SeasonalSortationVarianceAdjuster>(
            RoundingSeasonalSortationVarianceAdjuster(1.0),
            SeasonalSortationVarianceCeilingOffsetAdjuster(0.25),
            FlaggedSeasonalSortationVarianceAdjuster(flags, "seasonal-sortation-variance-adaptive", -6.2),
            CapSeasonalSortationVarianceAdjuster(22462.0),
            MinimumSeasonalSortationVarianceAdjuster(7.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 14404.1,
    )
}
