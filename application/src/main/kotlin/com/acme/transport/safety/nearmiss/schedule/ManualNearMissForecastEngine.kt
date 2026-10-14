package com.acme.transport.safety.nearmiss.schedule

import com.acme.transport.kit.numbers.roundTo

/** One step of the manual near miss forecast calculation. */
interface ManualNearMissForecastAdjuster {
    fun describe(): String
    fun adjust(item: ManualNearMissForecast, value: Double): Double
}

data class LabelManualNearMissForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualNearMissForecastAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: ManualNearMissForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ManualNearMissForecastQuantityOffsetAdjuster(var weight: Double) : ManualNearMissForecastAdjuster {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: ManualNearMissForecast, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class RoundingManualNearMissForecastAdjuster(var step: Double) : ManualNearMissForecastAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualNearMissForecast, value: Double): Double = roundTo(value, step)
}

data class TieredManualNearMissForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualNearMissForecastAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualNearMissForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual near miss forecast steps in order and keeps a trace of each. */
data class ManualNearMissForecastEngine(
    var steps: MutableList<ManualNearMissForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualNearMissForecast): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualNearMissForecastAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ManualNearMissForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualNearMissForecastEngine(): ManualNearMissForecastEngine {
    return ManualNearMissForecastEngine(
        steps = mutableListOf<ManualNearMissForecastAdjuster>(
            LabelManualNearMissForecastAdjuster(mutableMapOf<String, Double>("east" to 1.18)),
            ManualNearMissForecastQuantityOffsetAdjuster(0.61),
            RoundingManualNearMissForecastAdjuster(0.1),
            TieredManualNearMissForecastAdjuster(
                mutableListOf<Double>(755.0, 8230.0, 9628.0, 9728.0),
                mutableListOf<Double>(0.89, 1.11, 1.04, 1.09),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 35076.9,
    )
}
