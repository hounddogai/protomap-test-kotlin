package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched backhaul digest calculation. */
interface BatchedBackhaulDigestFactor {
    fun describe(): String
    fun adjust(item: BatchedBackhaulDigest, value: Double): Double
}

data class BatchedBackhaulDigestWeightOffsetAdjuster(var weight: Double) : BatchedBackhaulDigestFactor {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: BatchedBackhaulDigest, value: Double): Double = value + item.weight * weight
}

data class TitleBatchedBackhaulDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedBackhaulDigestFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: BatchedBackhaulDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumBatchedBackhaulDigestAdjuster(var minimum: Double) : BatchedBackhaulDigestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedBackhaulDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the batched backhaul digest steps in order and keeps a trace of each. */
data class BatchedBackhaulDigestEngine(
    var steps: MutableList<BatchedBackhaulDigestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedBackhaulDigest): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedBackhaulDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedBackhaulDigestEngine(): BatchedBackhaulDigestEngine {
    return BatchedBackhaulDigestEngine(
        steps = mutableListOf<BatchedBackhaulDigestFactor>(
            BatchedBackhaulDigestWeightOffsetAdjuster(0.21),
            TitleBatchedBackhaulDigestAdjuster(mutableMapOf<String, Double>("retail" to 1.32, "online" to 0.73)),
            MinimumBatchedBackhaulDigestAdjuster(15.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 40562.9,
    )
}
