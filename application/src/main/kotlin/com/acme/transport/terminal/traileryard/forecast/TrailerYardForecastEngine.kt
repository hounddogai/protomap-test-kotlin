package com.acme.transport.terminal.traileryard.forecast

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the trailer yard forecast calculation. */
interface TrailerYardForecastFactor {
    fun describe(): String
    fun adjust(item: TrailerYardForecast, value: Double): Double
}

data class CapTrailerYardForecastAdjuster(var cap: Double) : TrailerYardForecastFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerYardForecast, value: Double): Double = minOf(value, cap)
}

data class TrailerYardForecastRevisionOffsetAdjuster(var weight: Double) : TrailerYardForecastFactor {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: TrailerYardForecast, value: Double): Double = value + item.revision.toDouble() * weight
}

data class RoundingTrailerYardForecastAdjuster(var step: Double) : TrailerYardForecastFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerYardForecast, value: Double): Double = roundTo(value, step)
}

data class SegmentTrailerYardForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : TrailerYardForecastFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: TrailerYardForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredTrailerYardForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerYardForecastFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerYardForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentTrailerYardForecastAdjuster(var percent: Double) : TrailerYardForecastFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerYardForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the trailer yard forecast steps in order and keeps a trace of each. */
data class TrailerYardForecastEngine(
    var steps: MutableList<TrailerYardForecastFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TrailerYardForecast): Double {
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

    fun evaluateAll(items: MutableList<TrailerYardForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTrailerYardForecastEngine(): TrailerYardForecastEngine {
    return TrailerYardForecastEngine(
        steps = mutableListOf<TrailerYardForecastFactor>(
            CapTrailerYardForecastAdjuster(8010.0),
            TrailerYardForecastRevisionOffsetAdjuster(0.86),
            RoundingTrailerYardForecastAdjuster(1.0),
            SegmentTrailerYardForecastAdjuster(
                mutableMapOf<String, Double>("south" to 1.27, "north" to 1.21, "retail" to 1.05),
            ),
            TieredTrailerYardForecastAdjuster(
                mutableListOf<Double>(3536.0, 4595.0, 5144.0, 9296.0),
                mutableListOf<Double>(0.86, 0.9, 1.07, 0.84),
            ),
            PercentTrailerYardForecastAdjuster(-5.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 84948.5,
    )
}
