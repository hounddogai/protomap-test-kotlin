package com.acme.transport.fleetops.utilization.utilizationsegment

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback utilization summary calculation. */
interface FallbackUtilizationSummaryFactor {
    fun describe(): String
    fun adjust(item: FallbackUtilizationSummary, value: Double): Double
}

data class TieredFallbackUtilizationSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackUtilizationSummaryFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackUtilizationSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingFallbackUtilizationSummaryAdjuster(var step: Double) : FallbackUtilizationSummaryFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackUtilizationSummary, value: Double): Double = roundTo(value, step)
}

data class CapFallbackUtilizationSummaryAdjuster(var cap: Double) : FallbackUtilizationSummaryFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackUtilizationSummary, value: Double): Double = minOf(value, cap)
}

/** Runs the fallback utilization summary steps in order and keeps a trace of each. */
data class FallbackUtilizationSummaryEngine(
    var steps: MutableList<FallbackUtilizationSummaryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackUtilizationSummary): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackUtilizationSummaryFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackUtilizationSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackUtilizationSummaryEngine(): FallbackUtilizationSummaryEngine {
    return FallbackUtilizationSummaryEngine(
        steps = mutableListOf<FallbackUtilizationSummaryFactor>(
            TieredFallbackUtilizationSummaryAdjuster(
                mutableListOf<Double>(6747.0, 6797.0),
                mutableListOf<Double>(0.96, 1.01),
            ),
            RoundingFallbackUtilizationSummaryAdjuster(0.1),
            CapFallbackUtilizationSummaryAdjuster(15866.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 57686.0,
    )
}
