package com.acme.transport.emissions.fuelburn.notice

/** One step of the fuel burn estimate calculation. */
interface FuelBurnEstimateFactor {
    fun describe(): String
    fun adjust(item: FuelBurnEstimate, value: Double): Double
}

data class PercentFuelBurnEstimateAdjuster(var percent: Double) : FuelBurnEstimateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelBurnEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FuelBurnEstimateMaximumOffsetAdjuster(var weight: Double) : FuelBurnEstimateFactor {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: FuelBurnEstimate, value: Double): Double = value + item.maximum * weight
}

data class MinimumFuelBurnEstimateAdjuster(var minimum: Double) : FuelBurnEstimateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelBurnEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredFuelBurnEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelBurnEstimateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelBurnEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fuel burn estimate steps in order and keeps a trace of each. */
data class FuelBurnEstimateEngine(
    var steps: MutableList<FuelBurnEstimateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FuelBurnEstimate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelBurnEstimateFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FuelBurnEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFuelBurnEstimateEngine(): FuelBurnEstimateEngine {
    return FuelBurnEstimateEngine(
        steps = mutableListOf<FuelBurnEstimateFactor>(
            PercentFuelBurnEstimateAdjuster(-6.2),
            FuelBurnEstimateMaximumOffsetAdjuster(1.5),
            MinimumFuelBurnEstimateAdjuster(49.2),
            TieredFuelBurnEstimateAdjuster(
                mutableListOf<Double>(2894.0, 5147.0, 6245.0, 6699.0),
                mutableListOf<Double>(1.08, 1.04, 0.87, 1.0),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 85911.2,
    )
}
