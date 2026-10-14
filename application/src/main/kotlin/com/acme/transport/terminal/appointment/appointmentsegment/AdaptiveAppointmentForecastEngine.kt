package com.acme.transport.terminal.appointment.appointmentsegment

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive appointment forecast calculation. */
interface AdaptiveAppointmentForecastStep {
    fun describe(): String
    fun adjust(item: AdaptiveAppointmentForecast, value: Double): Double
}

data class CapAdaptiveAppointmentForecastAdjuster(var cap: Double) : AdaptiveAppointmentForecastStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveAppointmentForecast, value: Double): Double = minOf(value, cap)
}

data class NameAdaptiveAppointmentForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveAppointmentForecastStep {
    override fun describe(): String = "by name"

    override fun adjust(item: AdaptiveAppointmentForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class AdaptiveAppointmentForecastCostOffsetAdjuster(var weight: Double) : AdaptiveAppointmentForecastStep {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: AdaptiveAppointmentForecast, value: Double): Double = value + item.cost * weight
}

data class TieredAdaptiveAppointmentForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveAppointmentForecastStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveAppointmentForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the adaptive appointment forecast steps in order and keeps a trace of each. */
data class AdaptiveAppointmentForecastEngine(
    var steps: MutableList<AdaptiveAppointmentForecastStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveAppointmentForecast): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveAppointmentForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveAppointmentForecastEngine(): AdaptiveAppointmentForecastEngine {
    return AdaptiveAppointmentForecastEngine(
        steps = mutableListOf<AdaptiveAppointmentForecastStep>(
            CapAdaptiveAppointmentForecastAdjuster(26159.0),
            NameAdaptiveAppointmentForecastAdjuster(
                mutableMapOf<String, Double>("online" to 0.85, "central" to 1.26),
            ),
            AdaptiveAppointmentForecastCostOffsetAdjuster(0.68),
            TieredAdaptiveAppointmentForecastAdjuster(
                mutableListOf<Double>(2627.0, 7424.0),
                mutableListOf<Double>(0.87, 0.93),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 45422.8,
    )
}
