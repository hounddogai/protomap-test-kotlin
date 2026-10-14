package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional service area batch calculation. */
interface RegionalServiceAreaBatchStep {
    fun describe(): String
    fun adjust(item: RegionalServiceAreaBatch, value: Double): Double
}

data class CapRegionalServiceAreaBatchAdjuster(var cap: Double) : RegionalServiceAreaBatchStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalServiceAreaBatch, value: Double): Double = minOf(value, cap)
}

data class MinimumRegionalServiceAreaBatchAdjuster(var minimum: Double) : RegionalServiceAreaBatchStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalServiceAreaBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRegionalServiceAreaBatchAdjuster(var percent: Double) : RegionalServiceAreaBatchStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalServiceAreaBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRegionalServiceAreaBatchAdjuster(var step: Double) : RegionalServiceAreaBatchStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalServiceAreaBatch, value: Double): Double = roundTo(value, step)
}

/** Runs the regional service area batch steps in order and keeps a trace of each. */
data class RegionalServiceAreaBatchEngine(
    var steps: MutableList<RegionalServiceAreaBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalServiceAreaBatch): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalServiceAreaBatchStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalServiceAreaBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalServiceAreaBatchEngine(): RegionalServiceAreaBatchEngine {
    return RegionalServiceAreaBatchEngine(
        steps = mutableListOf<RegionalServiceAreaBatchStep>(
            CapRegionalServiceAreaBatchAdjuster(43564.0),
            MinimumRegionalServiceAreaBatchAdjuster(38.4),
            PercentRegionalServiceAreaBatchAdjuster(-22.8),
            RoundingRegionalServiceAreaBatchAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 95468.4,
    )
}
