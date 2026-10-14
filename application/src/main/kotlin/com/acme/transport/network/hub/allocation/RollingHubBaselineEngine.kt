package com.acme.transport.network.hub.allocation

import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling hub baseline calculation. */
interface RollingHubBaselineAdjuster {
    fun describe(): String
    fun adjust(item: RollingHubBaseline, value: Double): Double
}

data class RoundingRollingHubBaselineAdjuster(var step: Double) : RollingHubBaselineAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingHubBaseline, value: Double): Double = roundTo(value, step)
}

data class PercentRollingHubBaselineAdjuster(var percent: Double) : RollingHubBaselineAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingHubBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRollingHubBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingHubBaselineAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingHubBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRollingHubBaselineAdjuster(var cap: Double) : RollingHubBaselineAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingHubBaseline, value: Double): Double = minOf(value, cap)
}

/** Runs the rolling hub baseline steps in order and keeps a trace of each. */
data class RollingHubBaselineEngine(
    var steps: MutableList<RollingHubBaselineAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingHubBaseline): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingHubBaselineAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingHubBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingHubBaselineEngine(): RollingHubBaselineEngine {
    return RollingHubBaselineEngine(
        steps = mutableListOf<RollingHubBaselineAdjuster>(
            RoundingRollingHubBaselineAdjuster(0.25),
            PercentRollingHubBaselineAdjuster(7.3),
            TieredRollingHubBaselineAdjuster(
                mutableListOf<Double>(1948.0, 2225.0, 4118.0, 4219.0),
                mutableListOf<Double>(0.95, 1.01, 0.94, 0.84),
            ),
            CapRollingHubBaselineAdjuster(34800.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 68036.1,
    )
}
