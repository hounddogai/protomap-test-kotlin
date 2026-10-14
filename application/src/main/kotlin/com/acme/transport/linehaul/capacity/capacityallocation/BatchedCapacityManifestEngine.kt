package com.acme.transport.linehaul.capacity.capacityallocation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched capacity manifest calculation. */
interface BatchedCapacityManifestFactor {
    fun describe(): String
    fun adjust(item: BatchedCapacityManifest, value: Double): Double
}

data class MinimumBatchedCapacityManifestAdjuster(var minimum: Double) : BatchedCapacityManifestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedCapacityManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingBatchedCapacityManifestAdjuster(var step: Double) : BatchedCapacityManifestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedCapacityManifest, value: Double): Double = roundTo(value, step)
}

data class CapBatchedCapacityManifestAdjuster(var cap: Double) : BatchedCapacityManifestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedCapacityManifest, value: Double): Double = minOf(value, cap)
}

data class TieredBatchedCapacityManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedCapacityManifestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedCapacityManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameBatchedCapacityManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedCapacityManifestFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: BatchedCapacityManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class BatchedCapacityManifestLoadKgOffsetAdjuster(var weight: Double) : BatchedCapacityManifestFactor {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: BatchedCapacityManifest, value: Double): Double = value + item.loadKg * weight
}

/** Runs the batched capacity manifest steps in order and keeps a trace of each. */
data class BatchedCapacityManifestEngine(
    var steps: MutableList<BatchedCapacityManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedCapacityManifest): Double {
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

    fun evaluateAll(items: MutableList<BatchedCapacityManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedCapacityManifestEngine(): BatchedCapacityManifestEngine {
    return BatchedCapacityManifestEngine(
        steps = mutableListOf<BatchedCapacityManifestFactor>(
            MinimumBatchedCapacityManifestAdjuster(25.1),
            RoundingBatchedCapacityManifestAdjuster(0.25),
            CapBatchedCapacityManifestAdjuster(32628.0),
            TieredBatchedCapacityManifestAdjuster(
                mutableListOf<Double>(8323.0, 8838.0),
                mutableListOf<Double>(0.91, 1.19),
            ),
            NameBatchedCapacityManifestAdjuster(
                mutableMapOf<String, Double>("retail" to 0.88, "central" to 1.33, "west" to 0.92),
            ),
            BatchedCapacityManifestLoadKgOffsetAdjuster(0.02),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 50661.6,
    )
}
