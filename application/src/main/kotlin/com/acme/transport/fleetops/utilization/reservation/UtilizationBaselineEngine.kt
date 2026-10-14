package com.acme.transport.fleetops.utilization.reservation

import com.acme.transport.kit.numbers.roundTo

/** One step of the utilization baseline calculation. */
interface UtilizationBaselineAdjuster {
    fun describe(): String
    fun adjust(item: UtilizationBaseline, value: Double): Double
}

data class PercentUtilizationBaselineAdjuster(var percent: Double) : UtilizationBaselineAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: UtilizationBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumUtilizationBaselineAdjuster(var minimum: Double) : UtilizationBaselineAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: UtilizationBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingUtilizationBaselineAdjuster(var step: Double) : UtilizationBaselineAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: UtilizationBaseline, value: Double): Double = roundTo(value, step)
}

/** Runs the utilization baseline steps in order and keeps a trace of each. */
data class UtilizationBaselineEngine(
    var steps: MutableList<UtilizationBaselineAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: UtilizationBaseline): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: UtilizationBaselineAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<UtilizationBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultUtilizationBaselineEngine(): UtilizationBaselineEngine {
    return UtilizationBaselineEngine(
        steps = mutableListOf<UtilizationBaselineAdjuster>(
            PercentUtilizationBaselineAdjuster(-0.0),
            MinimumUtilizationBaselineAdjuster(1.2),
            RoundingUtilizationBaselineAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 69130.6,
    )
}
