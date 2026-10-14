package com.acme.transport.linehaul.trailer.preference

/** One step of the seasonal trailer adjustment calculation. */
interface SeasonalTrailerAdjustmentModifier {
    fun describe(): String
    fun adjust(item: SeasonalTrailerAdjustment, value: Double): Double
}

data class SeasonalTrailerAdjustmentLimitOffsetAdjuster(
    var weight: Double,
) : SeasonalTrailerAdjustmentModifier {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: SeasonalTrailerAdjustment, value: Double): Double {
        return value + item.limit.toDouble() * weight
    }
}

data class CapSeasonalTrailerAdjustmentAdjuster(var cap: Double) : SeasonalTrailerAdjustmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalTrailerAdjustment, value: Double): Double = minOf(value, cap)
}

data class TieredSeasonalTrailerAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalTrailerAdjustmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalTrailerAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumSeasonalTrailerAdjustmentAdjuster(var minimum: Double) : SeasonalTrailerAdjustmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalTrailerAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the seasonal trailer adjustment steps in order and keeps a trace of each. */
data class SeasonalTrailerAdjustmentEngine(
    var steps: MutableList<SeasonalTrailerAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalTrailerAdjustment): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalTrailerAdjustmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalTrailerAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalTrailerAdjustmentEngine(): SeasonalTrailerAdjustmentEngine {
    return SeasonalTrailerAdjustmentEngine(
        steps = mutableListOf<SeasonalTrailerAdjustmentModifier>(
            SeasonalTrailerAdjustmentLimitOffsetAdjuster(0.81),
            CapSeasonalTrailerAdjustmentAdjuster(38697.0),
            TieredSeasonalTrailerAdjustmentAdjuster(
                mutableListOf<Double>(2153.0, 6177.0),
                mutableListOf<Double>(1.02, 1.05),
            ),
            MinimumSeasonalTrailerAdjustmentAdjuster(22.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 95151.4,
    )
}
