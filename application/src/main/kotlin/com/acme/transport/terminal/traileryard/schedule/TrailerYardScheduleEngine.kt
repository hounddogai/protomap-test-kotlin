package com.acme.transport.terminal.traileryard.schedule

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the trailer yard schedule calculation. */
interface TrailerYardScheduleAdjuster {
    fun describe(): String
    fun adjust(item: TrailerYardSchedule, value: Double): Double
}

data class TieredTrailerYardScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerYardScheduleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerYardSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumTrailerYardScheduleAdjuster(var minimum: Double) : TrailerYardScheduleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerYardSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TrailerYardScheduleBudgetOffsetAdjuster(var weight: Double) : TrailerYardScheduleAdjuster {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: TrailerYardSchedule, value: Double): Double = value + item.budget * weight
}

data class RoundingTrailerYardScheduleAdjuster(var step: Double) : TrailerYardScheduleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerYardSchedule, value: Double): Double = roundTo(value, step)
}

data class PercentTrailerYardScheduleAdjuster(var percent: Double) : TrailerYardScheduleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerYardSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the trailer yard schedule steps in order and keeps a trace of each. */
data class TrailerYardScheduleEngine(
    var steps: MutableList<TrailerYardScheduleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TrailerYardSchedule): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TrailerYardSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTrailerYardScheduleEngine(): TrailerYardScheduleEngine {
    return TrailerYardScheduleEngine(
        steps = mutableListOf<TrailerYardScheduleAdjuster>(
            TieredTrailerYardScheduleAdjuster(
                mutableListOf<Double>(2630.0, 8832.0),
                mutableListOf<Double>(0.89, 0.87),
            ),
            MinimumTrailerYardScheduleAdjuster(45.8),
            TrailerYardScheduleBudgetOffsetAdjuster(0.65),
            RoundingTrailerYardScheduleAdjuster(1.0),
            PercentTrailerYardScheduleAdjuster(9.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 69517.8,
    )
}
