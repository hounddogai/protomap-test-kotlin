package com.acme.transport.freightpay.fuelsurcharge.surchargesummary

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict fuel surcharge summary calculation. */
interface StrictFuelSurchargeSummaryAdjuster {
    fun describe(): String
    fun adjust(item: StrictFuelSurchargeSummary, value: Double): Double
}

data class PercentStrictFuelSurchargeSummaryAdjuster(var percent: Double) : StrictFuelSurchargeSummaryAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictFuelSurchargeSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingStrictFuelSurchargeSummaryAdjuster(var step: Double) : StrictFuelSurchargeSummaryAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictFuelSurchargeSummary, value: Double): Double = roundTo(value, step)
}

data class TieredStrictFuelSurchargeSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictFuelSurchargeSummaryAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictFuelSurchargeSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict fuel surcharge summary steps in order and keeps a trace of each. */
data class StrictFuelSurchargeSummaryEngine(
    var steps: MutableList<StrictFuelSurchargeSummaryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictFuelSurchargeSummary): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictFuelSurchargeSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictFuelSurchargeSummaryEngine(): StrictFuelSurchargeSummaryEngine {
    return StrictFuelSurchargeSummaryEngine(
        steps = mutableListOf<StrictFuelSurchargeSummaryAdjuster>(
            PercentStrictFuelSurchargeSummaryAdjuster(-18.5),
            RoundingStrictFuelSurchargeSummaryAdjuster(0.05),
            TieredStrictFuelSurchargeSummaryAdjuster(
                mutableListOf<Double>(2827.0, 3584.0, 4549.0),
                mutableListOf<Double>(0.94, 0.89, 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 66240.6,
    )
}
