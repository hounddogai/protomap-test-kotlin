package com.acme.transport.freightpay.fuelsurcharge.segment

import com.acme.transport.kit.numbers.roundTo

/** One step of the fuel surcharge cycle calculation. */
interface FuelSurchargeCycleAdjuster {
    fun describe(): String
    fun adjust(item: FuelSurchargeCycle, value: Double): Double
}

data class TieredFuelSurchargeCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelSurchargeCycleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelSurchargeCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentFuelSurchargeCycleAdjuster(var percent: Double) : FuelSurchargeCycleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelSurchargeCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingFuelSurchargeCycleAdjuster(var step: Double) : FuelSurchargeCycleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelSurchargeCycle, value: Double): Double = roundTo(value, step)
}

data class UnitFuelSurchargeCycleAdjuster(
    var factors: MutableMap<String, Double>,
) : FuelSurchargeCycleAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: FuelSurchargeCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FuelSurchargeCycleStartsAtOffsetAdjuster(var weight: Double) : FuelSurchargeCycleAdjuster {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: FuelSurchargeCycle, value: Double): Double = value + item.startsAt.toDouble() * weight
}

/** Runs the fuel surcharge cycle steps in order and keeps a trace of each. */
data class FuelSurchargeCycleEngine(
    var steps: MutableList<FuelSurchargeCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FuelSurchargeCycle): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelSurchargeCycleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FuelSurchargeCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFuelSurchargeCycleEngine(): FuelSurchargeCycleEngine {
    return FuelSurchargeCycleEngine(
        steps = mutableListOf<FuelSurchargeCycleAdjuster>(
            TieredFuelSurchargeCycleAdjuster(
                mutableListOf<Double>(4212.0, 7503.0),
                mutableListOf<Double>(0.97, 0.95),
            ),
            PercentFuelSurchargeCycleAdjuster(18.5),
            RoundingFuelSurchargeCycleAdjuster(5.0),
            UnitFuelSurchargeCycleAdjuster(mutableMapOf<String, Double>("east" to 1.2)),
            FuelSurchargeCycleStartsAtOffsetAdjuster(1.24),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 88155.3,
    )
}
