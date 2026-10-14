package com.acme.transport.terminal.crossdock.target

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the cross dock checkpoint calculation. */
interface CrossDockCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: CrossDockCheckpoint, value: Double): Double
}

data class CrossDockCheckpointCeilingOffsetAdjuster(var weight: Double) : CrossDockCheckpointAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: CrossDockCheckpoint, value: Double): Double = value + item.ceiling * weight
}

data class LabelCrossDockCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : CrossDockCheckpointAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: CrossDockCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCrossDockCheckpointAdjuster(var step: Double) : CrossDockCheckpointAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CrossDockCheckpoint, value: Double): Double = roundTo(value, step)
}

data class TieredCrossDockCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CrossDockCheckpointAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CrossDockCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the cross dock checkpoint steps in order and keeps a trace of each. */
data class CrossDockCheckpointEngine(
    var steps: MutableList<CrossDockCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CrossDockCheckpoint): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CrossDockCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCrossDockCheckpointEngine(): CrossDockCheckpointEngine {
    return CrossDockCheckpointEngine(
        steps = mutableListOf<CrossDockCheckpointAdjuster>(
            CrossDockCheckpointCeilingOffsetAdjuster(1.5),
            LabelCrossDockCheckpointAdjuster(mutableMapOf<String, Double>("central" to 1.1)),
            RoundingCrossDockCheckpointAdjuster(0.25),
            TieredCrossDockCheckpointAdjuster(
                mutableListOf<Double>(6146.0, 9416.0),
                mutableListOf<Double>(0.93, 1.04),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 29146.2,
    )
}
