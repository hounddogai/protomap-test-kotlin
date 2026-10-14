package com.acme.transport.network.servicearea.areathreshold

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered service area forecast calculation. */
interface TieredServiceAreaForecastModifier {
    fun describe(): String
    fun adjust(item: TieredServiceAreaForecast, value: Double): Double
}

data class MinimumTieredServiceAreaForecastAdjuster(var minimum: Double) : TieredServiceAreaForecastModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredServiceAreaForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingTieredServiceAreaForecastAdjuster(var step: Double) : TieredServiceAreaForecastModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredServiceAreaForecast, value: Double): Double = roundTo(value, step)
}

data class PercentTieredServiceAreaForecastAdjuster(var percent: Double) : TieredServiceAreaForecastModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredServiceAreaForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapTieredServiceAreaForecastAdjuster(var cap: Double) : TieredServiceAreaForecastModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredServiceAreaForecast, value: Double): Double = minOf(value, cap)
}

data class LabelTieredServiceAreaForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredServiceAreaForecastModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: TieredServiceAreaForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tiered service area forecast steps in order and keeps a trace of each. */
data class TieredServiceAreaForecastEngine(
    var steps: MutableList<TieredServiceAreaForecastModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredServiceAreaForecast): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredServiceAreaForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredServiceAreaForecastEngine(): TieredServiceAreaForecastEngine {
    return TieredServiceAreaForecastEngine(
        steps = mutableListOf<TieredServiceAreaForecastModifier>(
            MinimumTieredServiceAreaForecastAdjuster(11.0),
            RoundingTieredServiceAreaForecastAdjuster(1.0),
            PercentTieredServiceAreaForecastAdjuster(24.2),
            CapTieredServiceAreaForecastAdjuster(15684.0),
            LabelTieredServiceAreaForecastAdjuster(mutableMapOf<String, Double>("retail" to 1.21)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 79501.3,
    )
}
