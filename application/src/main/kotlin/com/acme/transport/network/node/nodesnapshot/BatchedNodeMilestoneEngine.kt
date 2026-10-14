package com.acme.transport.network.node.nodesnapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched node milestone calculation. */
interface BatchedNodeMilestoneModifier {
    fun describe(): String
    fun adjust(item: BatchedNodeMilestone, value: Double): Double
}

data class MinimumBatchedNodeMilestoneAdjuster(var minimum: Double) : BatchedNodeMilestoneModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedNodeMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class BatchedNodeMilestoneFloorValueOffsetAdjuster(var weight: Double) : BatchedNodeMilestoneModifier {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: BatchedNodeMilestone, value: Double): Double = value + item.floorValue * weight
}

data class FlaggedBatchedNodeMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedNodeMilestoneModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedNodeMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentBatchedNodeMilestoneAdjuster(var percent: Double) : BatchedNodeMilestoneModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedNodeMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneBatchedNodeMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedNodeMilestoneModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: BatchedNodeMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredBatchedNodeMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedNodeMilestoneModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedNodeMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the batched node milestone steps in order and keeps a trace of each. */
data class BatchedNodeMilestoneEngine(
    var steps: MutableList<BatchedNodeMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedNodeMilestone): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedNodeMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedNodeMilestoneEngine(flags: Registry): BatchedNodeMilestoneEngine {
    return BatchedNodeMilestoneEngine(
        steps = mutableListOf<BatchedNodeMilestoneModifier>(
            MinimumBatchedNodeMilestoneAdjuster(49.7),
            BatchedNodeMilestoneFloorValueOffsetAdjuster(0.49),
            FlaggedBatchedNodeMilestoneAdjuster(flags, "batched-node-milestone-tiered", 2.9),
            PercentBatchedNodeMilestoneAdjuster(-14.3),
            LaneBatchedNodeMilestoneAdjuster(
                mutableMapOf<String, Double>("retail" to 0.71, "west" to 0.97, "south" to 0.95),
            ),
            TieredBatchedNodeMilestoneAdjuster(
                mutableListOf<Double>(122.0, 970.0, 6075.0, 6945.0),
                mutableListOf<Double>(0.8, 0.95, 1.11, 0.91),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 93987.9,
    )
}
