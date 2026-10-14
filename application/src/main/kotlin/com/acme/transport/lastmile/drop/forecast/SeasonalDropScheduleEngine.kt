package com.acme.transport.lastmile.drop.forecast

/** One step of the seasonal drop schedule calculation. */
interface SeasonalDropScheduleStep {
    fun describe(): String
    fun adjust(item: SeasonalDropSchedule, value: Double): Double
}

data class MinimumSeasonalDropScheduleAdjuster(var minimum: Double) : SeasonalDropScheduleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalDropSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapSeasonalDropScheduleAdjuster(var cap: Double) : SeasonalDropScheduleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalDropSchedule, value: Double): Double = minOf(value, cap)
}

data class PercentSeasonalDropScheduleAdjuster(var percent: Double) : SeasonalDropScheduleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalDropSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the seasonal drop schedule steps in order and keeps a trace of each. */
data class SeasonalDropScheduleEngine(
    var steps: MutableList<SeasonalDropScheduleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalDropSchedule): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalDropScheduleStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalDropSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalDropScheduleEngine(): SeasonalDropScheduleEngine {
    return SeasonalDropScheduleEngine(
        steps = mutableListOf<SeasonalDropScheduleStep>(
            MinimumSeasonalDropScheduleAdjuster(29.9),
            CapSeasonalDropScheduleAdjuster(8658.0),
            PercentSeasonalDropScheduleAdjuster(-0.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 77215.9,
    )
}
