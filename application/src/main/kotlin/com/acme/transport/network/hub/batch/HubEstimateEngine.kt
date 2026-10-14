package com.acme.transport.network.hub.batch

import com.acme.transport.kit.numbers.roundTo

/** One step of the hub estimate calculation. */
interface HubEstimateStep {
    fun describe(): String
    fun adjust(item: HubEstimate, value: Double): Double
}

data class RoundingHubEstimateAdjuster(var step: Double) : HubEstimateStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HubEstimate, value: Double): Double = roundTo(value, step)
}

data class PercentHubEstimateAdjuster(var percent: Double) : HubEstimateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HubEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapHubEstimateAdjuster(var cap: Double) : HubEstimateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HubEstimate, value: Double): Double = minOf(value, cap)
}

data class MinimumHubEstimateAdjuster(var minimum: Double) : HubEstimateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HubEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the hub estimate steps in order and keeps a trace of each. */
data class HubEstimateEngine(
    var steps: MutableList<HubEstimateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: HubEstimate): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HubEstimateStep = steps[index]
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

    fun evaluateAll(items: MutableList<HubEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultHubEstimateEngine(): HubEstimateEngine {
    return HubEstimateEngine(
        steps = mutableListOf<HubEstimateStep>(
            RoundingHubEstimateAdjuster(0.1),
            PercentHubEstimateAdjuster(19.8),
            CapHubEstimateAdjuster(1033.0),
            MinimumHubEstimateAdjuster(42.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 48536.8,
    )
}
