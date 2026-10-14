package com.acme.transport.safety.coaching.score

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched coaching digest calculation. */
interface BatchedCoachingDigestFactor {
    fun describe(): String
    fun adjust(item: BatchedCoachingDigest, value: Double): Double
}

data class BatchedCoachingDigestFloorValueOffsetAdjuster(var weight: Double) : BatchedCoachingDigestFactor {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: BatchedCoachingDigest, value: Double): Double = value + item.floorValue * weight
}

data class CapBatchedCoachingDigestAdjuster(var cap: Double) : BatchedCoachingDigestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedCoachingDigest, value: Double): Double = minOf(value, cap)
}

data class MinimumBatchedCoachingDigestAdjuster(var minimum: Double) : BatchedCoachingDigestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedCoachingDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the batched coaching digest steps in order and keeps a trace of each. */
data class BatchedCoachingDigestEngine(
    var steps: MutableList<BatchedCoachingDigestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedCoachingDigest): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedCoachingDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedCoachingDigestEngine(): BatchedCoachingDigestEngine {
    return BatchedCoachingDigestEngine(
        steps = mutableListOf<BatchedCoachingDigestFactor>(
            BatchedCoachingDigestFloorValueOffsetAdjuster(0.15),
            CapBatchedCoachingDigestAdjuster(12282.0),
            MinimumBatchedCoachingDigestAdjuster(36.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 98200.6,
    )
}
