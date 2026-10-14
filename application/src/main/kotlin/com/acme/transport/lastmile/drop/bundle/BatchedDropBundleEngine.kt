package com.acme.transport.lastmile.drop.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched drop bundle calculation. */
interface BatchedDropBundleStep {
    fun describe(): String
    fun adjust(item: BatchedDropBundle, value: Double): Double
}

data class BatchedDropBundlePriorityOffsetAdjuster(var weight: Double) : BatchedDropBundleStep {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: BatchedDropBundle, value: Double): Double = value + item.priority.toDouble() * weight
}

data class FlaggedBatchedDropBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedDropBundleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedDropBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentBatchedDropBundleAdjuster(var percent: Double) : BatchedDropBundleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedDropBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapBatchedDropBundleAdjuster(var cap: Double) : BatchedDropBundleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedDropBundle, value: Double): Double = minOf(value, cap)
}

/** Runs the batched drop bundle steps in order and keeps a trace of each. */
data class BatchedDropBundleEngine(
    var steps: MutableList<BatchedDropBundleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedDropBundle): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedDropBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedDropBundleEngine(flags: Registry): BatchedDropBundleEngine {
    return BatchedDropBundleEngine(
        steps = mutableListOf<BatchedDropBundleStep>(
            BatchedDropBundlePriorityOffsetAdjuster(0.83),
            FlaggedBatchedDropBundleAdjuster(flags, "batched-drop-bundle-primary", -6.6),
            PercentBatchedDropBundleAdjuster(-2.6),
            CapBatchedDropBundleAdjuster(23951.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 40517.8,
    )
}
