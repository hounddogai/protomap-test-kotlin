package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import com.acme.transport.kit.numbers.roundTo

/** One step of the fuel surcharge forecast calculation. */
interface FuelSurchargeForecastStep {
    fun describe(): String
    fun adjust(item: FuelSurchargeForecast, value: Double): Double
}

data class CapFuelSurchargeForecastAdjuster(var cap: Double) : FuelSurchargeForecastStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelSurchargeForecast, value: Double): Double = minOf(value, cap)
}

data class DepotFuelSurchargeForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : FuelSurchargeForecastStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: FuelSurchargeForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FuelSurchargeForecastRetriesOffsetAdjuster(var weight: Double) : FuelSurchargeForecastStep {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: FuelSurchargeForecast, value: Double): Double = value + item.retries.toDouble() * weight
}

data class TieredFuelSurchargeForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelSurchargeForecastStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelSurchargeForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingFuelSurchargeForecastAdjuster(var step: Double) : FuelSurchargeForecastStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelSurchargeForecast, value: Double): Double = roundTo(value, step)
}

/** Runs the fuel surcharge forecast steps in order and keeps a trace of each. */
data class FuelSurchargeForecastEngine(
    var steps: MutableList<FuelSurchargeForecastStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FuelSurchargeForecast): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelSurchargeForecastStep = steps[index]
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

    fun evaluateAll(items: MutableList<FuelSurchargeForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFuelSurchargeForecastEngine(): FuelSurchargeForecastEngine {
    return FuelSurchargeForecastEngine(
        steps = mutableListOf<FuelSurchargeForecastStep>(
            CapFuelSurchargeForecastAdjuster(11837.0),
            DepotFuelSurchargeForecastAdjuster(
                mutableMapOf<String, Double>("south" to 0.99, "north" to 0.95, "central" to 0.99),
            ),
            FuelSurchargeForecastRetriesOffsetAdjuster(1.6),
            TieredFuelSurchargeForecastAdjuster(
                mutableListOf<Double>(278.0, 715.0, 9017.0),
                mutableListOf<Double>(0.92, 0.97, 1.06),
            ),
            RoundingFuelSurchargeForecastAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 28251.9,
    )
}
