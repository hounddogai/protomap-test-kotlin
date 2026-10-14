package com.acme.transport.terminal.crossdock.cappeddockestimate

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched cross dock cycle calculation. */
interface BatchedCrossDockCycleAdjuster {
    fun describe(): String
    fun adjust(item: BatchedCrossDockCycle, value: Double): Double
}

data class CapBatchedCrossDockCycleAdjuster(var cap: Double) : BatchedCrossDockCycleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedCrossDockCycle, value: Double): Double = minOf(value, cap)
}

data class MinimumBatchedCrossDockCycleAdjuster(var minimum: Double) : BatchedCrossDockCycleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedCrossDockCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class BatchedCrossDockCycleAttemptsOffsetAdjuster(var weight: Double) : BatchedCrossDockCycleAdjuster {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: BatchedCrossDockCycle, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

data class TieredBatchedCrossDockCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedCrossDockCycleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedCrossDockCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the batched cross dock cycle steps in order and keeps a trace of each. */
data class BatchedCrossDockCycleEngine(
    var steps: MutableList<BatchedCrossDockCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedCrossDockCycle): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedCrossDockCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedCrossDockCycleEngine(): BatchedCrossDockCycleEngine {
    return BatchedCrossDockCycleEngine(
        steps = mutableListOf<BatchedCrossDockCycleAdjuster>(
            CapBatchedCrossDockCycleAdjuster(1491.0),
            MinimumBatchedCrossDockCycleAdjuster(45.7),
            BatchedCrossDockCycleAttemptsOffsetAdjuster(1.19),
            TieredBatchedCrossDockCycleAdjuster(
                mutableListOf<Double>(3072.0, 3292.0, 4314.0, 9265.0),
                mutableListOf<Double>(0.98, 1.09, 0.87, 0.83),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 80388.6,
    )
}
