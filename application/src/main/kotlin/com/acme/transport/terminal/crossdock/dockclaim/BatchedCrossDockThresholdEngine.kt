package com.acme.transport.terminal.crossdock.dockclaim

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched cross dock threshold calculation. */
interface BatchedCrossDockThresholdStep {
    fun describe(): String
    fun adjust(item: BatchedCrossDockThreshold, value: Double): Double
}

data class BatchedCrossDockThresholdWindowDaysOffsetAdjuster(
    var weight: Double,
) : BatchedCrossDockThresholdStep {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: BatchedCrossDockThreshold, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class PercentBatchedCrossDockThresholdAdjuster(var percent: Double) : BatchedCrossDockThresholdStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedCrossDockThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingBatchedCrossDockThresholdAdjuster(var step: Double) : BatchedCrossDockThresholdStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedCrossDockThreshold, value: Double): Double = roundTo(value, step)
}

data class CapBatchedCrossDockThresholdAdjuster(var cap: Double) : BatchedCrossDockThresholdStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedCrossDockThreshold, value: Double): Double = minOf(value, cap)
}

/** Runs the batched cross dock threshold steps in order and keeps a trace of each. */
data class BatchedCrossDockThresholdEngine(
    var steps: MutableList<BatchedCrossDockThresholdStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedCrossDockThreshold): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedCrossDockThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedCrossDockThresholdEngine(): BatchedCrossDockThresholdEngine {
    return BatchedCrossDockThresholdEngine(
        steps = mutableListOf<BatchedCrossDockThresholdStep>(
            BatchedCrossDockThresholdWindowDaysOffsetAdjuster(1.95),
            PercentBatchedCrossDockThresholdAdjuster(5.0),
            RoundingBatchedCrossDockThresholdAdjuster(1.0),
            CapBatchedCrossDockThresholdAdjuster(42065.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 78834.6,
    )
}
