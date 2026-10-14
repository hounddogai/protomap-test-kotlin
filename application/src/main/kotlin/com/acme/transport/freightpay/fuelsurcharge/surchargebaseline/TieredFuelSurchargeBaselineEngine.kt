package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered fuel surcharge baseline calculation. */
interface TieredFuelSurchargeBaselineStep {
    fun describe(): String
    fun adjust(item: TieredFuelSurchargeBaseline, value: Double): Double
}

data class CapTieredFuelSurchargeBaselineAdjuster(var cap: Double) : TieredFuelSurchargeBaselineStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredFuelSurchargeBaseline, value: Double): Double = minOf(value, cap)
}

data class RoundingTieredFuelSurchargeBaselineAdjuster(var step: Double) : TieredFuelSurchargeBaselineStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredFuelSurchargeBaseline, value: Double): Double = roundTo(value, step)
}

data class MinimumTieredFuelSurchargeBaselineAdjuster(var minimum: Double) : TieredFuelSurchargeBaselineStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredFuelSurchargeBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the tiered fuel surcharge baseline steps in order and keeps a trace of each. */
data class TieredFuelSurchargeBaselineEngine(
    var steps: MutableList<TieredFuelSurchargeBaselineStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredFuelSurchargeBaseline): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredFuelSurchargeBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredFuelSurchargeBaselineEngine(): TieredFuelSurchargeBaselineEngine {
    return TieredFuelSurchargeBaselineEngine(
        steps = mutableListOf<TieredFuelSurchargeBaselineStep>(
            CapTieredFuelSurchargeBaselineAdjuster(32481.0),
            RoundingTieredFuelSurchargeBaselineAdjuster(0.05),
            MinimumTieredFuelSurchargeBaselineAdjuster(5.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 42127.8,
    )
}
