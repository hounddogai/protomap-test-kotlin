package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal service plan budget calculation. */
interface SeasonalServicePlanBudgetModifier {
    fun describe(): String
    fun adjust(item: SeasonalServicePlanBudget, value: Double): Double
}

data class MinimumSeasonalServicePlanBudgetAdjuster(var minimum: Double) : SeasonalServicePlanBudgetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalServicePlanBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingSeasonalServicePlanBudgetAdjuster(var step: Double) : SeasonalServicePlanBudgetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalServicePlanBudget, value: Double): Double = roundTo(value, step)
}

data class CapSeasonalServicePlanBudgetAdjuster(var cap: Double) : SeasonalServicePlanBudgetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalServicePlanBudget, value: Double): Double = minOf(value, cap)
}

data class TieredSeasonalServicePlanBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalServicePlanBudgetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalServicePlanBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentSeasonalServicePlanBudgetAdjuster(var percent: Double) : SeasonalServicePlanBudgetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalServicePlanBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SeasonalServicePlanBudgetStopCountOffsetAdjuster(
    var weight: Double,
) : SeasonalServicePlanBudgetModifier {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: SeasonalServicePlanBudget, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

/** Runs the seasonal service plan budget steps in order and keeps a trace of each. */
data class SeasonalServicePlanBudgetEngine(
    var steps: MutableList<SeasonalServicePlanBudgetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalServicePlanBudget): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalServicePlanBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalServicePlanBudgetEngine(): SeasonalServicePlanBudgetEngine {
    return SeasonalServicePlanBudgetEngine(
        steps = mutableListOf<SeasonalServicePlanBudgetModifier>(
            MinimumSeasonalServicePlanBudgetAdjuster(2.7),
            RoundingSeasonalServicePlanBudgetAdjuster(1.0),
            CapSeasonalServicePlanBudgetAdjuster(6585.0),
            TieredSeasonalServicePlanBudgetAdjuster(
                mutableListOf<Double>(88.0, 6695.0, 9825.0),
                mutableListOf<Double>(1.05, 0.85, 0.95),
            ),
            PercentSeasonalServicePlanBudgetAdjuster(17.1),
            SeasonalServicePlanBudgetStopCountOffsetAdjuster(1.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 4301.7,
    )
}
