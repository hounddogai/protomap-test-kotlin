package com.acme.transport.customs.inspection.estimate

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient inspection estimate calculation. */
interface LenientInspectionEstimateFactor {
    fun describe(): String
    fun adjust(item: LenientInspectionEstimate, value: Double): Double
}

data class CapLenientInspectionEstimateAdjuster(var cap: Double) : LenientInspectionEstimateFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientInspectionEstimate, value: Double): Double = minOf(value, cap)
}

data class RoundingLenientInspectionEstimateAdjuster(var step: Double) : LenientInspectionEstimateFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientInspectionEstimate, value: Double): Double = roundTo(value, step)
}

data class TieredLenientInspectionEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientInspectionEstimateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientInspectionEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lenient inspection estimate steps in order and keeps a trace of each. */
data class LenientInspectionEstimateEngine(
    var steps: MutableList<LenientInspectionEstimateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientInspectionEstimate): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientInspectionEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientInspectionEstimateEngine(): LenientInspectionEstimateEngine {
    return LenientInspectionEstimateEngine(
        steps = mutableListOf<LenientInspectionEstimateFactor>(
            CapLenientInspectionEstimateAdjuster(46667.0),
            RoundingLenientInspectionEstimateAdjuster(5.0),
            TieredLenientInspectionEstimateAdjuster(
                mutableListOf<Double>(3374.0, 3395.0),
                mutableListOf<Double>(1.02, 1.04),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 26644.9,
    )
}
