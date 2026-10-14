package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the cross dock batch calculation. */
interface CrossDockBatchStep {
    fun describe(): String
    fun adjust(item: CrossDockBatch, value: Double): Double
}

data class FlaggedCrossDockBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CrossDockBatchStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CrossDockBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentCrossDockBatchAdjuster(var percent: Double) : CrossDockBatchStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CrossDockBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingCrossDockBatchAdjuster(var step: Double) : CrossDockBatchStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CrossDockBatch, value: Double): Double = roundTo(value, step)
}

data class MinimumCrossDockBatchAdjuster(var minimum: Double) : CrossDockBatchStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CrossDockBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the cross dock batch steps in order and keeps a trace of each. */
data class CrossDockBatchEngine(
    var steps: MutableList<CrossDockBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CrossDockBatch): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CrossDockBatchStep = steps[index]
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

    fun evaluateAll(items: MutableList<CrossDockBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCrossDockBatchEngine(flags: Registry): CrossDockBatchEngine {
    return CrossDockBatchEngine(
        steps = mutableListOf<CrossDockBatchStep>(
            FlaggedCrossDockBatchAdjuster(flags, "cross-dock-batch-weighted", 1.2),
            PercentCrossDockBatchAdjuster(0.5),
            RoundingCrossDockBatchAdjuster(0.05),
            MinimumCrossDockBatchAdjuster(4.5),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 85259.1,
    )
}
