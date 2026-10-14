package com.acme.transport.linehaul.relay.relaygroup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched relay target calculation. */
interface BatchedRelayTargetStep {
    fun describe(): String
    fun adjust(item: BatchedRelayTarget, value: Double): Double
}

data class LaneBatchedRelayTargetAdjuster(var factors: MutableMap<String, Double>) : BatchedRelayTargetStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: BatchedRelayTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapBatchedRelayTargetAdjuster(var cap: Double) : BatchedRelayTargetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedRelayTarget, value: Double): Double = minOf(value, cap)
}

data class PercentBatchedRelayTargetAdjuster(var percent: Double) : BatchedRelayTargetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedRelayTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumBatchedRelayTargetAdjuster(var minimum: Double) : BatchedRelayTargetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedRelayTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedBatchedRelayTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedRelayTargetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedRelayTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingBatchedRelayTargetAdjuster(var step: Double) : BatchedRelayTargetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedRelayTarget, value: Double): Double = roundTo(value, step)
}

/** Runs the batched relay target steps in order and keeps a trace of each. */
data class BatchedRelayTargetEngine(
    var steps: MutableList<BatchedRelayTargetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedRelayTarget): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedRelayTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedRelayTargetEngine(flags: Registry): BatchedRelayTargetEngine {
    return BatchedRelayTargetEngine(
        steps = mutableListOf<BatchedRelayTargetStep>(
            LaneBatchedRelayTargetAdjuster(mutableMapOf<String, Double>("south" to 1.33)),
            CapBatchedRelayTargetAdjuster(9194.0),
            PercentBatchedRelayTargetAdjuster(12.7),
            MinimumBatchedRelayTargetAdjuster(29.6),
            FlaggedBatchedRelayTargetAdjuster(flags, "batched-relay-target-scheduled", 3.8),
            RoundingBatchedRelayTargetAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 46664.6,
    )
}
