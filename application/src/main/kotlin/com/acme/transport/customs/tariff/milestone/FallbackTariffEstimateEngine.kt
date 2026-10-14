package com.acme.transport.customs.tariff.milestone

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback tariff estimate calculation. */
interface FallbackTariffEstimateModifier {
    fun describe(): String
    fun adjust(item: FallbackTariffEstimate, value: Double): Double
}

data class RoundingFallbackTariffEstimateAdjuster(var step: Double) : FallbackTariffEstimateModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackTariffEstimate, value: Double): Double = roundTo(value, step)
}

data class FallbackTariffEstimateCapacityOffsetAdjuster(var weight: Double) : FallbackTariffEstimateModifier {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: FallbackTariffEstimate, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class TieredFallbackTariffEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackTariffEstimateModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackTariffEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fallback tariff estimate steps in order and keeps a trace of each. */
data class FallbackTariffEstimateEngine(
    var steps: MutableList<FallbackTariffEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackTariffEstimate): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackTariffEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackTariffEstimateEngine(): FallbackTariffEstimateEngine {
    return FallbackTariffEstimateEngine(
        steps = mutableListOf<FallbackTariffEstimateModifier>(
            RoundingFallbackTariffEstimateAdjuster(5.0),
            FallbackTariffEstimateCapacityOffsetAdjuster(1.75),
            TieredFallbackTariffEstimateAdjuster(
                mutableListOf<Double>(3940.0, 7403.0, 8785.0),
                mutableListOf<Double>(1.01, 0.87, 1.18),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 96566.1,
    )
}
