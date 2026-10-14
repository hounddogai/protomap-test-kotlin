package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.kit.numbers.roundTo

/** One step of the dispute checkpoint calculation. */
interface DisputeCheckpointModifier {
    fun describe(): String
    fun adjust(item: DisputeCheckpoint, value: Double): Double
}

data class RoundingDisputeCheckpointAdjuster(var step: Double) : DisputeCheckpointModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DisputeCheckpoint, value: Double): Double = roundTo(value, step)
}

data class TieredDisputeCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DisputeCheckpointModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DisputeCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LaneDisputeCheckpointAdjuster(var factors: MutableMap<String, Double>) : DisputeCheckpointModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: DisputeCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumDisputeCheckpointAdjuster(var minimum: Double) : DisputeCheckpointModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DisputeCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the dispute checkpoint steps in order and keeps a trace of each. */
data class DisputeCheckpointEngine(
    var steps: MutableList<DisputeCheckpointModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DisputeCheckpoint): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DisputeCheckpointModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DisputeCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDisputeCheckpointEngine(): DisputeCheckpointEngine {
    return DisputeCheckpointEngine(
        steps = mutableListOf<DisputeCheckpointModifier>(
            RoundingDisputeCheckpointAdjuster(5.0),
            TieredDisputeCheckpointAdjuster(
                mutableListOf<Double>(1054.0, 3005.0, 6454.0),
                mutableListOf<Double>(1.01, 0.9, 1.03),
            ),
            LaneDisputeCheckpointAdjuster(mutableMapOf<String, Double>("retail" to 1.24, "south" to 0.99)),
            MinimumDisputeCheckpointAdjuster(32.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 97815.4,
    )
}
