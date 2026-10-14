package com.acme.transport.terminal.crossdock.cappeddockestimate

import com.acme.transport.kit.numbers.roundTo

/** One step of the capped cross dock estimate calculation. */
interface CappedCrossDockEstimateFactor {
    fun describe(): String
    fun adjust(item: CappedCrossDockEstimate, value: Double): Double
}

data class RoundingCappedCrossDockEstimateAdjuster(var step: Double) : CappedCrossDockEstimateFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedCrossDockEstimate, value: Double): Double = roundTo(value, step)
}

data class CappedCrossDockEstimateWindowDaysOffsetAdjuster(var weight: Double) : CappedCrossDockEstimateFactor {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: CappedCrossDockEstimate, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class PercentCappedCrossDockEstimateAdjuster(var percent: Double) : CappedCrossDockEstimateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedCrossDockEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapCappedCrossDockEstimateAdjuster(var cap: Double) : CappedCrossDockEstimateFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedCrossDockEstimate, value: Double): Double = minOf(value, cap)
}

data class TieredCappedCrossDockEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedCrossDockEstimateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedCrossDockEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped cross dock estimate steps in order and keeps a trace of each. */
data class CappedCrossDockEstimateEngine(
    var steps: MutableList<CappedCrossDockEstimateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedCrossDockEstimate): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedCrossDockEstimateFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CappedCrossDockEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedCrossDockEstimateEngine(): CappedCrossDockEstimateEngine {
    return CappedCrossDockEstimateEngine(
        steps = mutableListOf<CappedCrossDockEstimateFactor>(
            RoundingCappedCrossDockEstimateAdjuster(0.5),
            CappedCrossDockEstimateWindowDaysOffsetAdjuster(0.4),
            PercentCappedCrossDockEstimateAdjuster(24.2),
            CapCappedCrossDockEstimateAdjuster(35556.0),
            TieredCappedCrossDockEstimateAdjuster(
                mutableListOf<Double>(3579.0, 5649.0),
                mutableListOf<Double>(0.97, 0.86),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 28636.0,
    )
}
