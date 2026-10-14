package com.acme.transport.freightpay.fuelsurcharge.window

import com.acme.transport.kit.numbers.roundTo

/** One step of the global fuel surcharge baseline calculation. */
interface GlobalFuelSurchargeBaselineStep {
    fun describe(): String
    fun adjust(item: GlobalFuelSurchargeBaseline, value: Double): Double
}

data class GlobalFuelSurchargeBaselineVersionOffsetAdjuster(
    var weight: Double,
) : GlobalFuelSurchargeBaselineStep {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: GlobalFuelSurchargeBaseline, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class RoundingGlobalFuelSurchargeBaselineAdjuster(var step: Double) : GlobalFuelSurchargeBaselineStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalFuelSurchargeBaseline, value: Double): Double = roundTo(value, step)
}

data class CapGlobalFuelSurchargeBaselineAdjuster(var cap: Double) : GlobalFuelSurchargeBaselineStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalFuelSurchargeBaseline, value: Double): Double = minOf(value, cap)
}

/** Runs the global fuel surcharge baseline steps in order and keeps a trace of each. */
data class GlobalFuelSurchargeBaselineEngine(
    var steps: MutableList<GlobalFuelSurchargeBaselineStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalFuelSurchargeBaseline): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalFuelSurchargeBaselineStep = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalFuelSurchargeBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalFuelSurchargeBaselineEngine(): GlobalFuelSurchargeBaselineEngine {
    return GlobalFuelSurchargeBaselineEngine(
        steps = mutableListOf<GlobalFuelSurchargeBaselineStep>(
            GlobalFuelSurchargeBaselineVersionOffsetAdjuster(0.22),
            RoundingGlobalFuelSurchargeBaselineAdjuster(0.5),
            CapGlobalFuelSurchargeBaselineAdjuster(31151.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 95544.9,
    )
}
