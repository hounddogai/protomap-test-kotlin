package com.acme.transport.terminal.sortation.adjustment

import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal sortation adjustment calculation. */
interface SeasonalSortationAdjustmentFactor {
    fun describe(): String
    fun adjust(item: SeasonalSortationAdjustment, value: Double): Double
}

data class MinimumSeasonalSortationAdjustmentAdjuster(var minimum: Double) : SeasonalSortationAdjustmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalSortationAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SeasonalSortationAdjustmentExpiresAtOffsetAdjuster(
    var weight: Double,
) : SeasonalSortationAdjustmentFactor {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: SeasonalSortationAdjustment, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

data class RoundingSeasonalSortationAdjustmentAdjuster(var step: Double) : SeasonalSortationAdjustmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalSortationAdjustment, value: Double): Double = roundTo(value, step)
}

data class PercentSeasonalSortationAdjustmentAdjuster(var percent: Double) : SeasonalSortationAdjustmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalSortationAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LocaleSeasonalSortationAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalSortationAdjustmentFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: SeasonalSortationAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredSeasonalSortationAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalSortationAdjustmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalSortationAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the seasonal sortation adjustment steps in order and keeps a trace of each. */
data class SeasonalSortationAdjustmentEngine(
    var steps: MutableList<SeasonalSortationAdjustmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalSortationAdjustment): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalSortationAdjustmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalSortationAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalSortationAdjustmentEngine(): SeasonalSortationAdjustmentEngine {
    return SeasonalSortationAdjustmentEngine(
        steps = mutableListOf<SeasonalSortationAdjustmentFactor>(
            MinimumSeasonalSortationAdjustmentAdjuster(6.9),
            SeasonalSortationAdjustmentExpiresAtOffsetAdjuster(1.9),
            RoundingSeasonalSortationAdjustmentAdjuster(5.0),
            PercentSeasonalSortationAdjustmentAdjuster(21.5),
            LocaleSeasonalSortationAdjustmentAdjuster(
                mutableMapOf<String, Double>("east" to 0.71, "retail" to 0.93),
            ),
            TieredSeasonalSortationAdjustmentAdjuster(
                mutableListOf<Double>(740.0, 1140.0, 8545.0, 9453.0),
                mutableListOf<Double>(0.93, 1.19, 0.82, 1.06),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 44235.8,
    )
}
