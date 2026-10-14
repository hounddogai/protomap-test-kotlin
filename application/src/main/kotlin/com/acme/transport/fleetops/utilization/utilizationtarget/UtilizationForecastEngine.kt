package com.acme.transport.fleetops.utilization.utilizationtarget

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the utilization forecast calculation. */
interface UtilizationForecastModifier {
    fun describe(): String
    fun adjust(item: UtilizationForecast, value: Double): Double
}

data class PercentUtilizationForecastAdjuster(var percent: Double) : UtilizationForecastModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: UtilizationForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelUtilizationForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : UtilizationForecastModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: UtilizationForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class UtilizationForecastEndsAtOffsetAdjuster(var weight: Double) : UtilizationForecastModifier {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: UtilizationForecast, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class CapUtilizationForecastAdjuster(var cap: Double) : UtilizationForecastModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: UtilizationForecast, value: Double): Double = minOf(value, cap)
}

data class MinimumUtilizationForecastAdjuster(var minimum: Double) : UtilizationForecastModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: UtilizationForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the utilization forecast steps in order and keeps a trace of each. */
data class UtilizationForecastEngine(
    var steps: MutableList<UtilizationForecastModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: UtilizationForecast): Double {
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

    fun evaluateAll(items: MutableList<UtilizationForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultUtilizationForecastEngine(): UtilizationForecastEngine {
    return UtilizationForecastEngine(
        steps = mutableListOf<UtilizationForecastModifier>(
            PercentUtilizationForecastAdjuster(-1.7),
            LabelUtilizationForecastAdjuster(
                mutableMapOf<String, Double>("east" to 0.72, "online" to 1.23, "south" to 0.71),
            ),
            UtilizationForecastEndsAtOffsetAdjuster(1.86),
            CapUtilizationForecastAdjuster(6546.0),
            MinimumUtilizationForecastAdjuster(45.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 28128.4,
    )
}
