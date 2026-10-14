package com.acme.transport.fleetops.serviceplan.legacyplanrollup

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual service plan forecast calculation. */
interface ManualServicePlanForecastAdjuster {
    fun describe(): String
    fun adjust(item: ManualServicePlanForecast, value: Double): Double
}

data class ManualServicePlanForecastWindowDaysOffsetAdjuster(
    var weight: Double,
) : ManualServicePlanForecastAdjuster {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: ManualServicePlanForecast, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class MinimumManualServicePlanForecastAdjuster(var minimum: Double) : ManualServicePlanForecastAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualServicePlanForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredManualServicePlanForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualServicePlanForecastAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualServicePlanForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual service plan forecast steps in order and keeps a trace of each. */
data class ManualServicePlanForecastEngine(
    var steps: MutableList<ManualServicePlanForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualServicePlanForecast): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualServicePlanForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualServicePlanForecastEngine(): ManualServicePlanForecastEngine {
    return ManualServicePlanForecastEngine(
        steps = mutableListOf<ManualServicePlanForecastAdjuster>(
            ManualServicePlanForecastWindowDaysOffsetAdjuster(1.9),
            MinimumManualServicePlanForecastAdjuster(48.9),
            TieredManualServicePlanForecastAdjuster(
                mutableListOf<Double>(5341.0, 5929.0, 9551.0),
                mutableListOf<Double>(1.0, 1.14, 0.97),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 83887.6,
    )
}
