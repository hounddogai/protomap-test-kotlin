package com.acme.transport.fleetops.serviceplan.metric

import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy service plan forecast calculation. */
interface LegacyServicePlanForecastAdjuster {
    fun describe(): String
    fun adjust(item: LegacyServicePlanForecast, value: Double): Double
}

data class TieredLegacyServicePlanForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyServicePlanForecastAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyServicePlanForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapLegacyServicePlanForecastAdjuster(var cap: Double) : LegacyServicePlanForecastAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyServicePlanForecast, value: Double): Double = minOf(value, cap)
}

data class LegacyServicePlanForecastFloorValueOffsetAdjuster(
    var weight: Double,
) : LegacyServicePlanForecastAdjuster {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: LegacyServicePlanForecast, value: Double): Double = value + item.floorValue * weight
}

data class TitleLegacyServicePlanForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyServicePlanForecastAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: LegacyServicePlanForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingLegacyServicePlanForecastAdjuster(var step: Double) : LegacyServicePlanForecastAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyServicePlanForecast, value: Double): Double = roundTo(value, step)
}

/** Runs the legacy service plan forecast steps in order and keeps a trace of each. */
data class LegacyServicePlanForecastEngine(
    var steps: MutableList<LegacyServicePlanForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyServicePlanForecast): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyServicePlanForecastAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyServicePlanForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyServicePlanForecastEngine(): LegacyServicePlanForecastEngine {
    return LegacyServicePlanForecastEngine(
        steps = mutableListOf<LegacyServicePlanForecastAdjuster>(
            TieredLegacyServicePlanForecastAdjuster(
                mutableListOf<Double>(2621.0, 3096.0),
                mutableListOf<Double>(1.16, 1.11),
            ),
            CapLegacyServicePlanForecastAdjuster(14509.0),
            LegacyServicePlanForecastFloorValueOffsetAdjuster(1.99),
            TitleLegacyServicePlanForecastAdjuster(
                mutableMapOf<String, Double>("online" to 0.89, "west" to 0.86, "central" to 1.26),
            ),
            RoundingLegacyServicePlanForecastAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 77247.9,
    )
}
