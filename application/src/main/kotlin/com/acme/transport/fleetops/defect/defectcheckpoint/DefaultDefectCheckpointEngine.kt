package com.acme.transport.fleetops.defect.defectcheckpoint

import com.acme.transport.kit.numbers.roundTo

/** One step of the default defect checkpoint calculation. */
interface DefaultDefectCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: DefaultDefectCheckpoint, value: Double): Double
}

data class DefaultDefectCheckpointMarginOffsetAdjuster(var weight: Double) : DefaultDefectCheckpointAdjuster {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: DefaultDefectCheckpoint, value: Double): Double = value + item.margin * weight
}

data class TitleDefaultDefectCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultDefectCheckpointAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: DefaultDefectCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumDefaultDefectCheckpointAdjuster(var minimum: Double) : DefaultDefectCheckpointAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultDefectCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingDefaultDefectCheckpointAdjuster(var step: Double) : DefaultDefectCheckpointAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultDefectCheckpoint, value: Double): Double = roundTo(value, step)
}

/** Runs the default defect checkpoint steps in order and keeps a trace of each. */
data class DefaultDefectCheckpointEngine(
    var steps: MutableList<DefaultDefectCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultDefectCheckpoint): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultDefectCheckpointAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultDefectCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultDefectCheckpointEngine(): DefaultDefectCheckpointEngine {
    return DefaultDefectCheckpointEngine(
        steps = mutableListOf<DefaultDefectCheckpointAdjuster>(
            DefaultDefectCheckpointMarginOffsetAdjuster(1.41),
            TitleDefaultDefectCheckpointAdjuster(mutableMapOf<String, Double>("east" to 0.78)),
            MinimumDefaultDefectCheckpointAdjuster(24.0),
            RoundingDefaultDefectCheckpointAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 8121.9,
    )
}
