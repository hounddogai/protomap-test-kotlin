package com.acme.transport.linehaul.relay.checkpoint

import com.acme.transport.kit.numbers.roundTo

/** One step of the relay checkpoint calculation. */
interface RelayCheckpointModifier {
    fun describe(): String
    fun adjust(item: RelayCheckpoint, value: Double): Double
}

data class CapRelayCheckpointAdjuster(var cap: Double) : RelayCheckpointModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RelayCheckpoint, value: Double): Double = minOf(value, cap)
}

data class TitleRelayCheckpointAdjuster(var factors: MutableMap<String, Double>) : RelayCheckpointModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: RelayCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRelayCheckpointAdjuster(var step: Double) : RelayCheckpointModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RelayCheckpoint, value: Double): Double = roundTo(value, step)
}

data class TieredRelayCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RelayCheckpointModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RelayCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRelayCheckpointAdjuster(var minimum: Double) : RelayCheckpointModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RelayCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RelayCheckpointRetriesOffsetAdjuster(var weight: Double) : RelayCheckpointModifier {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: RelayCheckpoint, value: Double): Double = value + item.retries.toDouble() * weight
}

/** Runs the relay checkpoint steps in order and keeps a trace of each. */
data class RelayCheckpointEngine(
    var steps: MutableList<RelayCheckpointModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RelayCheckpoint): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RelayCheckpointModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RelayCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRelayCheckpointEngine(): RelayCheckpointEngine {
    return RelayCheckpointEngine(
        steps = mutableListOf<RelayCheckpointModifier>(
            CapRelayCheckpointAdjuster(24480.0),
            TitleRelayCheckpointAdjuster(mutableMapOf<String, Double>("retail" to 1.04)),
            RoundingRelayCheckpointAdjuster(0.05),
            TieredRelayCheckpointAdjuster(
                mutableListOf<Double>(1963.0, 4827.0, 8133.0),
                mutableListOf<Double>(0.89, 1.11, 0.91),
            ),
            MinimumRelayCheckpointAdjuster(14.6),
            RelayCheckpointRetriesOffsetAdjuster(1.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 45258.3,
    )
}
