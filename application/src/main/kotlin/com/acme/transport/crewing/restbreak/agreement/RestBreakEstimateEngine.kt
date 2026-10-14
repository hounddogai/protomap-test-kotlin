package com.acme.transport.crewing.restbreak.agreement

import com.acme.transport.kit.numbers.roundTo

/** One step of the rest break estimate calculation. */
interface RestBreakEstimateModifier {
    fun describe(): String
    fun adjust(item: RestBreakEstimate, value: Double): Double
}

data class RoundingRestBreakEstimateAdjuster(var step: Double) : RestBreakEstimateModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RestBreakEstimate, value: Double): Double = roundTo(value, step)
}

data class RestBreakEstimateWindowDaysOffsetAdjuster(var weight: Double) : RestBreakEstimateModifier {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: RestBreakEstimate, value: Double): Double = value + item.windowDays.toDouble() * weight
}

data class PercentRestBreakEstimateAdjuster(var percent: Double) : RestBreakEstimateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RestBreakEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRestBreakEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RestBreakEstimateModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RestBreakEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rest break estimate steps in order and keeps a trace of each. */
data class RestBreakEstimateEngine(
    var steps: MutableList<RestBreakEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RestBreakEstimate): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RestBreakEstimateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RestBreakEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRestBreakEstimateEngine(): RestBreakEstimateEngine {
    return RestBreakEstimateEngine(
        steps = mutableListOf<RestBreakEstimateModifier>(
            RoundingRestBreakEstimateAdjuster(1.0),
            RestBreakEstimateWindowDaysOffsetAdjuster(1.09),
            PercentRestBreakEstimateAdjuster(12.5),
            TieredRestBreakEstimateAdjuster(
                mutableListOf<Double>(8315.0, 8839.0),
                mutableListOf<Double>(1.17, 0.84),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 90795.8,
    )
}
