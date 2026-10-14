package com.acme.transport.freightpay.fuelsurcharge.audit

/** One step of the fuel surcharge metric calculation. */
interface FuelSurchargeMetricModifier {
    fun describe(): String
    fun adjust(item: FuelSurchargeMetric, value: Double): Double
}

data class TieredFuelSurchargeMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelSurchargeMetricModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelSurchargeMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FuelSurchargeMetricMaximumOffsetAdjuster(var weight: Double) : FuelSurchargeMetricModifier {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: FuelSurchargeMetric, value: Double): Double = value + item.maximum * weight
}

data class DepotFuelSurchargeMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : FuelSurchargeMetricModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: FuelSurchargeMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumFuelSurchargeMetricAdjuster(var minimum: Double) : FuelSurchargeMetricModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelSurchargeMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapFuelSurchargeMetricAdjuster(var cap: Double) : FuelSurchargeMetricModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelSurchargeMetric, value: Double): Double = minOf(value, cap)
}

/** Runs the fuel surcharge metric steps in order and keeps a trace of each. */
data class FuelSurchargeMetricEngine(
    var steps: MutableList<FuelSurchargeMetricModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FuelSurchargeMetric): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelSurchargeMetricModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FuelSurchargeMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFuelSurchargeMetricEngine(): FuelSurchargeMetricEngine {
    return FuelSurchargeMetricEngine(
        steps = mutableListOf<FuelSurchargeMetricModifier>(
            TieredFuelSurchargeMetricAdjuster(
                mutableListOf<Double>(1073.0, 5836.0),
                mutableListOf<Double>(1.18, 0.86),
            ),
            FuelSurchargeMetricMaximumOffsetAdjuster(1.18),
            DepotFuelSurchargeMetricAdjuster(mutableMapOf<String, Double>("east" to 1.2, "south" to 1.05)),
            MinimumFuelSurchargeMetricAdjuster(15.0),
            CapFuelSurchargeMetricAdjuster(46497.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 62498.4,
    )
}
