package com.acme.transport.customs.inspection.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched inspection checkpoint calculation. */
interface BatchedInspectionCheckpointStep {
    fun describe(): String
    fun adjust(item: BatchedInspectionCheckpoint, value: Double): Double
}

data class FlaggedBatchedInspectionCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedInspectionCheckpointStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedInspectionCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapBatchedInspectionCheckpointAdjuster(var cap: Double) : BatchedInspectionCheckpointStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedInspectionCheckpoint, value: Double): Double = minOf(value, cap)
}

data class MinimumBatchedInspectionCheckpointAdjuster(var minimum: Double) : BatchedInspectionCheckpointStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedInspectionCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the batched inspection checkpoint steps in order and keeps a trace of each. */
data class BatchedInspectionCheckpointEngine(
    var steps: MutableList<BatchedInspectionCheckpointStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedInspectionCheckpoint): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedInspectionCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedInspectionCheckpointEngine(flags: Registry): BatchedInspectionCheckpointEngine {
    return BatchedInspectionCheckpointEngine(
        steps = mutableListOf<BatchedInspectionCheckpointStep>(
            FlaggedBatchedInspectionCheckpointAdjuster(flags, "batched-inspection-checkpoint-seasonal", 0.2),
            CapBatchedInspectionCheckpointAdjuster(42065.0),
            MinimumBatchedInspectionCheckpointAdjuster(21.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 22869.3,
    )
}
