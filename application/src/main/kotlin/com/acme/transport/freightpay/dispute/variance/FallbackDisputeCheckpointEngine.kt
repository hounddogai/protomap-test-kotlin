package com.acme.transport.freightpay.dispute.variance

import com.acme.transport.kit.flags.Registry

/** One step of the fallback dispute checkpoint calculation. */
interface FallbackDisputeCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: FallbackDisputeCheckpoint, value: Double): Double
}

data class FallbackDisputeCheckpointThresholdOffsetAdjuster(
    var weight: Double,
) : FallbackDisputeCheckpointAdjuster {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: FallbackDisputeCheckpoint, value: Double): Double = value + item.threshold * weight
}

data class CapFallbackDisputeCheckpointAdjuster(var cap: Double) : FallbackDisputeCheckpointAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackDisputeCheckpoint, value: Double): Double = minOf(value, cap)
}

data class FlaggedFallbackDisputeCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackDisputeCheckpointAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackDisputeCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleFallbackDisputeCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackDisputeCheckpointAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: FallbackDisputeCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredFallbackDisputeCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackDisputeCheckpointAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackDisputeCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fallback dispute checkpoint steps in order and keeps a trace of each. */
data class FallbackDisputeCheckpointEngine(
    var steps: MutableList<FallbackDisputeCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackDisputeCheckpoint): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackDisputeCheckpointAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackDisputeCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackDisputeCheckpointEngine(flags: Registry): FallbackDisputeCheckpointEngine {
    return FallbackDisputeCheckpointEngine(
        steps = mutableListOf<FallbackDisputeCheckpointAdjuster>(
            FallbackDisputeCheckpointThresholdOffsetAdjuster(1.59),
            CapFallbackDisputeCheckpointAdjuster(48537.0),
            FlaggedFallbackDisputeCheckpointAdjuster(flags, "fallback-dispute-checkpoint-batched", 8.6),
            TitleFallbackDisputeCheckpointAdjuster(mutableMapOf<String, Double>("retail" to 1.25)),
            TieredFallbackDisputeCheckpointAdjuster(
                mutableListOf<Double>(5607.0, 8122.0, 8619.0),
                mutableListOf<Double>(0.83, 1.2, 1.18),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 45262.7,
    )
}
