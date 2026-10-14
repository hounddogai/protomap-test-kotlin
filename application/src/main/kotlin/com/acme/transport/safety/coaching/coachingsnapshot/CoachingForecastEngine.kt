package com.acme.transport.safety.coaching.coachingsnapshot

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the coaching forecast calculation. */
interface CoachingForecastFactor {
    fun describe(): String
    fun adjust(item: CoachingForecast, value: Double): Double
}

data class CapCoachingForecastAdjuster(var cap: Double) : CoachingForecastFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CoachingForecast, value: Double): Double = minOf(value, cap)
}

data class TieredCoachingForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CoachingForecastFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CoachingForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleCoachingForecastAdjuster(var factors: MutableMap<String, Double>) : CoachingForecastFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: CoachingForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the coaching forecast steps in order and keeps a trace of each. */
data class CoachingForecastEngine(
    var steps: MutableList<CoachingForecastFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CoachingForecast): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CoachingForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCoachingForecastEngine(): CoachingForecastEngine {
    return CoachingForecastEngine(
        steps = mutableListOf<CoachingForecastFactor>(
            CapCoachingForecastAdjuster(13374.0),
            TieredCoachingForecastAdjuster(
                mutableListOf<Double>(4012.0, 5399.0, 6615.0, 7933.0),
                mutableListOf<Double>(0.88, 1.01, 1.04, 1.02),
            ),
            TitleCoachingForecastAdjuster(
                mutableMapOf<String, Double>("retail" to 0.85, "north" to 1.04, "east" to 0.88),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 96022.2,
    )
}
