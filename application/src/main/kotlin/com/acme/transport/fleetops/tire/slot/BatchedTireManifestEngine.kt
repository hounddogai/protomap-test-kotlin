package com.acme.transport.fleetops.tire.slot

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched tire manifest calculation. */
interface BatchedTireManifestFactor {
    fun describe(): String
    fun adjust(item: BatchedTireManifest, value: Double): Double
}

data class RoundingBatchedTireManifestAdjuster(var step: Double) : BatchedTireManifestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedTireManifest, value: Double): Double = roundTo(value, step)
}

data class PercentBatchedTireManifestAdjuster(var percent: Double) : BatchedTireManifestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedTireManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapBatchedTireManifestAdjuster(var cap: Double) : BatchedTireManifestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedTireManifest, value: Double): Double = minOf(value, cap)
}

/** Runs the batched tire manifest steps in order and keeps a trace of each. */
data class BatchedTireManifestEngine(
    var steps: MutableList<BatchedTireManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedTireManifest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedTireManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedTireManifestEngine(): BatchedTireManifestEngine {
    return BatchedTireManifestEngine(
        steps = mutableListOf<BatchedTireManifestFactor>(
            RoundingBatchedTireManifestAdjuster(0.05),
            PercentBatchedTireManifestAdjuster(14.8),
            CapBatchedTireManifestAdjuster(981.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 39237.4,
    )
}
