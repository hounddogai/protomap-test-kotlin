package com.acme.transport.emissions.idling.idlingtarget

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the seasonal idling profile calculation. */
interface SeasonalIdlingProfileFactor {
    fun describe(): String
    fun adjust(item: SeasonalIdlingProfile, value: Double): Double
}

data class SeasonalIdlingProfileBudgetOffsetAdjuster(var weight: Double) : SeasonalIdlingProfileFactor {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: SeasonalIdlingProfile, value: Double): Double = value + item.budget * weight
}

data class CapSeasonalIdlingProfileAdjuster(var cap: Double) : SeasonalIdlingProfileFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalIdlingProfile, value: Double): Double = minOf(value, cap)
}

data class TieredSeasonalIdlingProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalIdlingProfileFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalIdlingProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumSeasonalIdlingProfileAdjuster(var minimum: Double) : SeasonalIdlingProfileFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalIdlingProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the seasonal idling profile steps in order and keeps a trace of each. */
data class SeasonalIdlingProfileEngine(
    var steps: MutableList<SeasonalIdlingProfileFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalIdlingProfile): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalIdlingProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalIdlingProfileEngine(): SeasonalIdlingProfileEngine {
    return SeasonalIdlingProfileEngine(
        steps = mutableListOf<SeasonalIdlingProfileFactor>(
            SeasonalIdlingProfileBudgetOffsetAdjuster(0.2),
            CapSeasonalIdlingProfileAdjuster(39379.0),
            TieredSeasonalIdlingProfileAdjuster(
                mutableListOf<Double>(325.0, 2425.0, 3551.0),
                mutableListOf<Double>(0.97, 1.0, 1.18),
            ),
            MinimumSeasonalIdlingProfileAdjuster(35.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 54285.5,
    )
}
