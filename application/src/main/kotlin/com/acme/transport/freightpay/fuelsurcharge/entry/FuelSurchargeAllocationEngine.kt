package com.acme.transport.freightpay.fuelsurcharge.entry

import com.acme.transport.kit.numbers.roundTo

/** One step of the fuel surcharge allocation calculation. */
interface FuelSurchargeAllocationFactor {
    fun describe(): String
    fun adjust(item: FuelSurchargeAllocation, value: Double): Double
}

data class RoundingFuelSurchargeAllocationAdjuster(var step: Double) : FuelSurchargeAllocationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelSurchargeAllocation, value: Double): Double = roundTo(value, step)
}

data class CapFuelSurchargeAllocationAdjuster(var cap: Double) : FuelSurchargeAllocationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelSurchargeAllocation, value: Double): Double = minOf(value, cap)
}

data class TieredFuelSurchargeAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelSurchargeAllocationFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelSurchargeAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FuelSurchargeAllocationRateOffsetAdjuster(var weight: Double) : FuelSurchargeAllocationFactor {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: FuelSurchargeAllocation, value: Double): Double = value + item.rate * weight
}

data class LocaleFuelSurchargeAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : FuelSurchargeAllocationFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: FuelSurchargeAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fuel surcharge allocation steps in order and keeps a trace of each. */
data class FuelSurchargeAllocationEngine(
    var steps: MutableList<FuelSurchargeAllocationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FuelSurchargeAllocation): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelSurchargeAllocationFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FuelSurchargeAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFuelSurchargeAllocationEngine(): FuelSurchargeAllocationEngine {
    return FuelSurchargeAllocationEngine(
        steps = mutableListOf<FuelSurchargeAllocationFactor>(
            RoundingFuelSurchargeAllocationAdjuster(0.05),
            CapFuelSurchargeAllocationAdjuster(18808.0),
            TieredFuelSurchargeAllocationAdjuster(
                mutableListOf<Double>(1985.0, 4029.0, 6055.0),
                mutableListOf<Double>(0.93, 0.93, 0.98),
            ),
            FuelSurchargeAllocationRateOffsetAdjuster(0.6),
            LocaleFuelSurchargeAllocationAdjuster(mutableMapOf<String, Double>("central" to 1.24)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 98137.6,
    )
}
