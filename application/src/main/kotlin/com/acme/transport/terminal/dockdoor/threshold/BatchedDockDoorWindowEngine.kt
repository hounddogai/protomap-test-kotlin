package com.acme.transport.terminal.dockdoor.threshold

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched dock door window calculation. */
interface BatchedDockDoorWindowAdjuster {
    fun describe(): String
    fun adjust(item: BatchedDockDoorWindow, value: Double): Double
}

data class TieredBatchedDockDoorWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedDockDoorWindowAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedDockDoorWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class BatchedDockDoorWindowPriorityOffsetAdjuster(var weight: Double) : BatchedDockDoorWindowAdjuster {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: BatchedDockDoorWindow, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

data class RoundingBatchedDockDoorWindowAdjuster(var step: Double) : BatchedDockDoorWindowAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedDockDoorWindow, value: Double): Double = roundTo(value, step)
}

/** Runs the batched dock door window steps in order and keeps a trace of each. */
data class BatchedDockDoorWindowEngine(
    var steps: MutableList<BatchedDockDoorWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedDockDoorWindow): Double {
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

    fun evaluateAll(items: MutableList<BatchedDockDoorWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedDockDoorWindowEngine(): BatchedDockDoorWindowEngine {
    return BatchedDockDoorWindowEngine(
        steps = mutableListOf<BatchedDockDoorWindowAdjuster>(
            TieredBatchedDockDoorWindowAdjuster(
                mutableListOf<Double>(7058.0, 8557.0, 8629.0),
                mutableListOf<Double>(1.18, 1.03, 1.05),
            ),
            BatchedDockDoorWindowPriorityOffsetAdjuster(0.19),
            RoundingBatchedDockDoorWindowAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 53415.2,
    )
}
