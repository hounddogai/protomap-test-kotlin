package com.acme.transport.terminal.dockdoor.checkpoint

import com.acme.transport.kit.flags.Registry

/** One step of the batched dock door baseline calculation. */
interface BatchedDockDoorBaselineFactor {
    fun describe(): String
    fun adjust(item: BatchedDockDoorBaseline, value: Double): Double
}

data class MinimumBatchedDockDoorBaselineAdjuster(var minimum: Double) : BatchedDockDoorBaselineFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedDockDoorBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedBatchedDockDoorBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedDockDoorBaselineFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedDockDoorBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleBatchedDockDoorBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedDockDoorBaselineFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: BatchedDockDoorBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the batched dock door baseline steps in order and keeps a trace of each. */
data class BatchedDockDoorBaselineEngine(
    var steps: MutableList<BatchedDockDoorBaselineFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedDockDoorBaseline): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedDockDoorBaselineFactor = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedDockDoorBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedDockDoorBaselineEngine(flags: Registry): BatchedDockDoorBaselineEngine {
    return BatchedDockDoorBaselineEngine(
        steps = mutableListOf<BatchedDockDoorBaselineFactor>(
            MinimumBatchedDockDoorBaselineAdjuster(20.6),
            FlaggedBatchedDockDoorBaselineAdjuster(flags, "batched-dock-door-baseline-tiered", 8.1),
            TitleBatchedDockDoorBaselineAdjuster(
                mutableMapOf<String, Double>("west" to 0.78, "east" to 1.37, "retail" to 1.18),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 57929.1,
    )
}
