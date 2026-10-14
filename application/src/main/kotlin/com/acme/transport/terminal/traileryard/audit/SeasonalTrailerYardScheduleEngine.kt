package com.acme.transport.terminal.traileryard.audit

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal trailer yard schedule calculation. */
interface SeasonalTrailerYardScheduleFactor {
    fun describe(): String
    fun adjust(item: SeasonalTrailerYardSchedule, value: Double): Double
}

data class MinimumSeasonalTrailerYardScheduleAdjuster(var minimum: Double) : SeasonalTrailerYardScheduleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalTrailerYardSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SeasonalTrailerYardScheduleStartsAtOffsetAdjuster(
    var weight: Double,
) : SeasonalTrailerYardScheduleFactor {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: SeasonalTrailerYardSchedule, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class PercentSeasonalTrailerYardScheduleAdjuster(var percent: Double) : SeasonalTrailerYardScheduleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalTrailerYardSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredSeasonalTrailerYardScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalTrailerYardScheduleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalTrailerYardSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingSeasonalTrailerYardScheduleAdjuster(var step: Double) : SeasonalTrailerYardScheduleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalTrailerYardSchedule, value: Double): Double = roundTo(value, step)
}

data class CategorySeasonalTrailerYardScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalTrailerYardScheduleFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: SeasonalTrailerYardSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the seasonal trailer yard schedule steps in order and keeps a trace of each. */
data class SeasonalTrailerYardScheduleEngine(
    var steps: MutableList<SeasonalTrailerYardScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalTrailerYardSchedule): Double {
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

    fun evaluateAll(items: MutableList<SeasonalTrailerYardSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalTrailerYardScheduleEngine(): SeasonalTrailerYardScheduleEngine {
    return SeasonalTrailerYardScheduleEngine(
        steps = mutableListOf<SeasonalTrailerYardScheduleFactor>(
            MinimumSeasonalTrailerYardScheduleAdjuster(27.1),
            SeasonalTrailerYardScheduleStartsAtOffsetAdjuster(1.94),
            PercentSeasonalTrailerYardScheduleAdjuster(8.1),
            TieredSeasonalTrailerYardScheduleAdjuster(
                mutableListOf<Double>(704.0, 5533.0),
                mutableListOf<Double>(0.91, 0.83),
            ),
            RoundingSeasonalTrailerYardScheduleAdjuster(0.25),
            CategorySeasonalTrailerYardScheduleAdjuster(
                mutableMapOf<String, Double>("north" to 1.14, "south" to 1.17, "west" to 1.32),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 87800.1,
    )
}
