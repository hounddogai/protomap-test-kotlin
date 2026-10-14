package com.acme.transport.emissions.idling.idlingwindow

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched idling threshold calculation. */
interface BatchedIdlingThresholdFactor {
    fun describe(): String
    fun adjust(item: BatchedIdlingThreshold, value: Double): Double
}

data class FlaggedBatchedIdlingThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedIdlingThresholdFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedIdlingThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingBatchedIdlingThresholdAdjuster(var step: Double) : BatchedIdlingThresholdFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedIdlingThreshold, value: Double): Double = roundTo(value, step)
}

data class BatchedIdlingThresholdBatchSizeOffsetAdjuster(var weight: Double) : BatchedIdlingThresholdFactor {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: BatchedIdlingThreshold, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class LaneBatchedIdlingThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedIdlingThresholdFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: BatchedIdlingThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapBatchedIdlingThresholdAdjuster(var cap: Double) : BatchedIdlingThresholdFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedIdlingThreshold, value: Double): Double = minOf(value, cap)
}

/** Runs the batched idling threshold steps in order and keeps a trace of each. */
data class BatchedIdlingThresholdEngine(
    var steps: MutableList<BatchedIdlingThresholdFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedIdlingThreshold): Double {
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

    fun evaluateAll(items: MutableList<BatchedIdlingThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedIdlingThresholdEngine(flags: Registry): BatchedIdlingThresholdEngine {
    return BatchedIdlingThresholdEngine(
        steps = mutableListOf<BatchedIdlingThresholdFactor>(
            FlaggedBatchedIdlingThresholdAdjuster(flags, "batched-idling-threshold-fallback", -4.0),
            RoundingBatchedIdlingThresholdAdjuster(0.1),
            BatchedIdlingThresholdBatchSizeOffsetAdjuster(1.53),
            LaneBatchedIdlingThresholdAdjuster(mutableMapOf<String, Double>("retail" to 1.39, "south" to 0.79)),
            CapBatchedIdlingThresholdAdjuster(17740.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 10335.8,
    )
}
