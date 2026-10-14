package com.acme.transport.linehaul.trailer.trailerbatch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal trailer batch calculation. */
interface SeasonalTrailerBatchAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalTrailerBatch, value: Double): Double
}

data class TitleSeasonalTrailerBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalTrailerBatchAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: SeasonalTrailerBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedSeasonalTrailerBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalTrailerBatchAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalTrailerBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumSeasonalTrailerBatchAdjuster(var minimum: Double) : SeasonalTrailerBatchAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalTrailerBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingSeasonalTrailerBatchAdjuster(var step: Double) : SeasonalTrailerBatchAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalTrailerBatch, value: Double): Double = roundTo(value, step)
}

data class PercentSeasonalTrailerBatchAdjuster(var percent: Double) : SeasonalTrailerBatchAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalTrailerBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapSeasonalTrailerBatchAdjuster(var cap: Double) : SeasonalTrailerBatchAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalTrailerBatch, value: Double): Double = minOf(value, cap)
}

/** Runs the seasonal trailer batch steps in order and keeps a trace of each. */
data class SeasonalTrailerBatchEngine(
    var steps: MutableList<SeasonalTrailerBatchAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalTrailerBatch): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalTrailerBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalTrailerBatchEngine(flags: Registry): SeasonalTrailerBatchEngine {
    return SeasonalTrailerBatchEngine(
        steps = mutableListOf<SeasonalTrailerBatchAdjuster>(
            TitleSeasonalTrailerBatchAdjuster(mutableMapOf<String, Double>("west" to 1.15)),
            FlaggedSeasonalTrailerBatchAdjuster(flags, "seasonal-trailer-batch-manual", 3.8),
            MinimumSeasonalTrailerBatchAdjuster(46.6),
            RoundingSeasonalTrailerBatchAdjuster(5.0),
            PercentSeasonalTrailerBatchAdjuster(-20.4),
            CapSeasonalTrailerBatchAdjuster(35995.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 37514.6,
    )
}
