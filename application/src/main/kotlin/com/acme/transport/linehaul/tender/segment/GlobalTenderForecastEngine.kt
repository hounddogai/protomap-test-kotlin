package com.acme.transport.linehaul.tender.segment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global tender forecast calculation. */
interface GlobalTenderForecastFactor {
    fun describe(): String
    fun adjust(item: GlobalTenderForecast, value: Double): Double
}

data class RoundingGlobalTenderForecastAdjuster(var step: Double) : GlobalTenderForecastFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalTenderForecast, value: Double): Double = roundTo(value, step)
}

data class MinimumGlobalTenderForecastAdjuster(var minimum: Double) : GlobalTenderForecastFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalTenderForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelGlobalTenderForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalTenderForecastFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: GlobalTenderForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class GlobalTenderForecastScoreOffsetAdjuster(var weight: Double) : GlobalTenderForecastFactor {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: GlobalTenderForecast, value: Double): Double = value + item.score * weight
}

/** Runs the global tender forecast steps in order and keeps a trace of each. */
data class GlobalTenderForecastEngine(
    var steps: MutableList<GlobalTenderForecastFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalTenderForecast): Double {
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

    fun evaluateAll(items: MutableList<GlobalTenderForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalTenderForecastEngine(): GlobalTenderForecastEngine {
    return GlobalTenderForecastEngine(
        steps = mutableListOf<GlobalTenderForecastFactor>(
            RoundingGlobalTenderForecastAdjuster(0.05),
            MinimumGlobalTenderForecastAdjuster(8.7),
            LabelGlobalTenderForecastAdjuster(mutableMapOf<String, Double>("online" to 1.13, "south" to 1.21)),
            GlobalTenderForecastScoreOffsetAdjuster(0.98),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 17343.5,
    )
}
