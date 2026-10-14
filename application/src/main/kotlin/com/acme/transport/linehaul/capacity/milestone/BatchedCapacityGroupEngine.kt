package com.acme.transport.linehaul.capacity.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched capacity group calculation. */
interface BatchedCapacityGroupModifier {
    fun describe(): String
    fun adjust(item: BatchedCapacityGroup, value: Double): Double
}

data class FlaggedBatchedCapacityGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedCapacityGroupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedCapacityGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapBatchedCapacityGroupAdjuster(var cap: Double) : BatchedCapacityGroupModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedCapacityGroup, value: Double): Double = minOf(value, cap)
}

data class PercentBatchedCapacityGroupAdjuster(var percent: Double) : BatchedCapacityGroupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedCapacityGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class BatchedCapacityGroupExpiresAtOffsetAdjuster(var weight: Double) : BatchedCapacityGroupModifier {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: BatchedCapacityGroup, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

/** Runs the batched capacity group steps in order and keeps a trace of each. */
data class BatchedCapacityGroupEngine(
    var steps: MutableList<BatchedCapacityGroupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedCapacityGroup): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedCapacityGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedCapacityGroupEngine(flags: Registry): BatchedCapacityGroupEngine {
    return BatchedCapacityGroupEngine(
        steps = mutableListOf<BatchedCapacityGroupModifier>(
            FlaggedBatchedCapacityGroupAdjuster(flags, "batched-capacity-group-pending", -1.7),
            CapBatchedCapacityGroupAdjuster(8628.0),
            PercentBatchedCapacityGroupAdjuster(-6.7),
            BatchedCapacityGroupExpiresAtOffsetAdjuster(0.11),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 58989.1,
    )
}
