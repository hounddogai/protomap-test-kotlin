package com.acme.transport.fleetops.utilization.summary

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the utilization summary calculation. */
interface UtilizationSummaryStep {
    fun describe(): String
    fun adjust(item: UtilizationSummary, value: Double): Double
}

data class MinimumUtilizationSummaryAdjuster(var minimum: Double) : UtilizationSummaryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: UtilizationSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingUtilizationSummaryAdjuster(var step: Double) : UtilizationSummaryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: UtilizationSummary, value: Double): Double = roundTo(value, step)
}

data class LabelUtilizationSummaryAdjuster(var factors: MutableMap<String, Double>) : UtilizationSummaryStep {
    override fun describe(): String = "by label"

    override fun adjust(item: UtilizationSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredUtilizationSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : UtilizationSummaryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: UtilizationSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the utilization summary steps in order and keeps a trace of each. */
data class UtilizationSummaryEngine(
    var steps: MutableList<UtilizationSummaryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: UtilizationSummary): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<UtilizationSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultUtilizationSummaryEngine(): UtilizationSummaryEngine {
    return UtilizationSummaryEngine(
        steps = mutableListOf<UtilizationSummaryStep>(
            MinimumUtilizationSummaryAdjuster(3.6),
            RoundingUtilizationSummaryAdjuster(5.0),
            LabelUtilizationSummaryAdjuster(
                mutableMapOf<String, Double>("north" to 0.85, "retail" to 1.1, "online" to 0.76),
            ),
            TieredUtilizationSummaryAdjuster(
                mutableListOf<Double>(5311.0, 5312.0, 5452.0),
                mutableListOf<Double>(1.08, 1.07, 0.87),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 68196.9,
    )
}
