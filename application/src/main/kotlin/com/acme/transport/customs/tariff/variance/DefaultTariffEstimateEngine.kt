package com.acme.transport.customs.tariff.variance

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default tariff estimate calculation. */
interface DefaultTariffEstimateFactor {
    fun describe(): String
    fun adjust(item: DefaultTariffEstimate, value: Double): Double
}

data class MinimumDefaultTariffEstimateAdjuster(var minimum: Double) : DefaultTariffEstimateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultTariffEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DefaultTariffEstimateAmountOffsetAdjuster(var weight: Double) : DefaultTariffEstimateFactor {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: DefaultTariffEstimate, value: Double): Double = value + item.amount * weight
}

data class TieredDefaultTariffEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultTariffEstimateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultTariffEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the default tariff estimate steps in order and keeps a trace of each. */
data class DefaultTariffEstimateEngine(
    var steps: MutableList<DefaultTariffEstimateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultTariffEstimate): Double {
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

    fun evaluateAll(items: MutableList<DefaultTariffEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultTariffEstimateEngine(): DefaultTariffEstimateEngine {
    return DefaultTariffEstimateEngine(
        steps = mutableListOf<DefaultTariffEstimateFactor>(
            MinimumDefaultTariffEstimateAdjuster(33.8),
            DefaultTariffEstimateAmountOffsetAdjuster(0.46),
            TieredDefaultTariffEstimateAdjuster(
                mutableListOf<Double>(4293.0, 7481.0),
                mutableListOf<Double>(1.17, 1.16),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 3785.2,
    )
}
