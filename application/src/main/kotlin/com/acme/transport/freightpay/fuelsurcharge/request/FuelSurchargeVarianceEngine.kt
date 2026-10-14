package com.acme.transport.freightpay.fuelsurcharge.request

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fuel surcharge variance calculation. */
interface FuelSurchargeVarianceFactor {
    fun describe(): String
    fun adjust(item: FuelSurchargeVariance, value: Double): Double
}

data class PercentFuelSurchargeVarianceAdjuster(var percent: Double) : FuelSurchargeVarianceFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelSurchargeVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapFuelSurchargeVarianceAdjuster(var cap: Double) : FuelSurchargeVarianceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelSurchargeVariance, value: Double): Double = minOf(value, cap)
}

data class RoundingFuelSurchargeVarianceAdjuster(var step: Double) : FuelSurchargeVarianceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelSurchargeVariance, value: Double): Double = roundTo(value, step)
}

/** Runs the fuel surcharge variance steps in order and keeps a trace of each. */
data class FuelSurchargeVarianceEngine(
    var steps: MutableList<FuelSurchargeVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FuelSurchargeVariance): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FuelSurchargeVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFuelSurchargeVarianceEngine(): FuelSurchargeVarianceEngine {
    return FuelSurchargeVarianceEngine(
        steps = mutableListOf<FuelSurchargeVarianceFactor>(
            PercentFuelSurchargeVarianceAdjuster(-10.8),
            CapFuelSurchargeVarianceAdjuster(32102.0),
            RoundingFuelSurchargeVarianceAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 73893.2,
    )
}
