package com.acme.transport.network.hub.metric

import com.acme.transport.kit.numbers.roundTo

/** One step of the manual hub baseline calculation. */
interface ManualHubBaselineAdjuster {
    fun describe(): String
    fun adjust(item: ManualHubBaseline, value: Double): Double
}

data class TitleManualHubBaselineAdjuster(var factors: MutableMap<String, Double>) : ManualHubBaselineAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: ManualHubBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingManualHubBaselineAdjuster(var step: Double) : ManualHubBaselineAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualHubBaseline, value: Double): Double = roundTo(value, step)
}

data class ManualHubBaselinePriorityOffsetAdjuster(var weight: Double) : ManualHubBaselineAdjuster {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: ManualHubBaseline, value: Double): Double = value + item.priority.toDouble() * weight
}

data class TieredManualHubBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualHubBaselineAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualHubBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual hub baseline steps in order and keeps a trace of each. */
data class ManualHubBaselineEngine(
    var steps: MutableList<ManualHubBaselineAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualHubBaseline): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualHubBaselineAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ManualHubBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualHubBaselineEngine(): ManualHubBaselineEngine {
    return ManualHubBaselineEngine(
        steps = mutableListOf<ManualHubBaselineAdjuster>(
            TitleManualHubBaselineAdjuster(
                mutableMapOf<String, Double>("west" to 1.34, "retail" to 1.0, "south" to 1.18),
            ),
            RoundingManualHubBaselineAdjuster(1.0),
            ManualHubBaselinePriorityOffsetAdjuster(1.6),
            TieredManualHubBaselineAdjuster(
                mutableListOf<Double>(5105.0, 8551.0, 9293.0),
                mutableListOf<Double>(0.91, 1.08, 1.2),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 14635.4,
    )
}
