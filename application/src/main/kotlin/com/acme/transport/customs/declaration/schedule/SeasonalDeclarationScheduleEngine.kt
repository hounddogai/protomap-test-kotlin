package com.acme.transport.customs.declaration.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal declaration schedule calculation. */
interface SeasonalDeclarationScheduleModifier {
    fun describe(): String
    fun adjust(item: SeasonalDeclarationSchedule, value: Double): Double
}

data class CapSeasonalDeclarationScheduleAdjuster(var cap: Double) : SeasonalDeclarationScheduleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalDeclarationSchedule, value: Double): Double = minOf(value, cap)
}

data class TieredSeasonalDeclarationScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalDeclarationScheduleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalDeclarationSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedSeasonalDeclarationScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalDeclarationScheduleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalDeclarationSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingSeasonalDeclarationScheduleAdjuster(var step: Double) : SeasonalDeclarationScheduleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalDeclarationSchedule, value: Double): Double = roundTo(value, step)
}

data class MinimumSeasonalDeclarationScheduleAdjuster(
    var minimum: Double,
) : SeasonalDeclarationScheduleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalDeclarationSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentSeasonalDeclarationScheduleAdjuster(
    var percent: Double,
) : SeasonalDeclarationScheduleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalDeclarationSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the seasonal declaration schedule steps in order and keeps a trace of each. */
data class SeasonalDeclarationScheduleEngine(
    var steps: MutableList<SeasonalDeclarationScheduleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalDeclarationSchedule): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalDeclarationScheduleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalDeclarationSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalDeclarationScheduleEngine(flags: Registry): SeasonalDeclarationScheduleEngine {
    return SeasonalDeclarationScheduleEngine(
        steps = mutableListOf<SeasonalDeclarationScheduleModifier>(
            CapSeasonalDeclarationScheduleAdjuster(41563.0),
            TieredSeasonalDeclarationScheduleAdjuster(
                mutableListOf<Double>(4235.0, 8145.0),
                mutableListOf<Double>(0.92, 1.03),
            ),
            FlaggedSeasonalDeclarationScheduleAdjuster(flags, "seasonal-declaration-schedule-scheduled", -3.2),
            RoundingSeasonalDeclarationScheduleAdjuster(0.1),
            MinimumSeasonalDeclarationScheduleAdjuster(31.2),
            PercentSeasonalDeclarationScheduleAdjuster(-8.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 46841.5,
    )
}
