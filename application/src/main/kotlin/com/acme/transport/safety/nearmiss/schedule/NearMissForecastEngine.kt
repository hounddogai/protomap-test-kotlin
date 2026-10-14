package com.acme.transport.safety.nearmiss.schedule

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the near miss forecast calculation. */
interface NearMissForecastStep {
    fun describe(): String
    fun adjust(item: NearMissForecast, value: Double): Double
}

data class NearMissForecastPriorityOffsetAdjuster(var weight: Double) : NearMissForecastStep {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: NearMissForecast, value: Double): Double = value + item.priority.toDouble() * weight
}

data class TitleNearMissForecastAdjuster(var factors: MutableMap<String, Double>) : NearMissForecastStep {
    override fun describe(): String = "by title"

    override fun adjust(item: NearMissForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumNearMissForecastAdjuster(var minimum: Double) : NearMissForecastStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NearMissForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredNearMissForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NearMissForecastStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NearMissForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the near miss forecast steps in order and keeps a trace of each. */
data class NearMissForecastEngine(
    var steps: MutableList<NearMissForecastStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: NearMissForecast): Double {
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

    fun evaluateAll(items: MutableList<NearMissForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultNearMissForecastEngine(): NearMissForecastEngine {
    return NearMissForecastEngine(
        steps = mutableListOf<NearMissForecastStep>(
            NearMissForecastPriorityOffsetAdjuster(0.29),
            TitleNearMissForecastAdjuster(
                mutableMapOf<String, Double>("east" to 1.31, "central" to 0.76, "online" to 1.26),
            ),
            MinimumNearMissForecastAdjuster(26.2),
            TieredNearMissForecastAdjuster(
                mutableListOf<Double>(3785.0, 6357.0, 7289.0),
                mutableListOf<Double>(1.13, 1.1, 0.91),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 42270.0,
    )
}
