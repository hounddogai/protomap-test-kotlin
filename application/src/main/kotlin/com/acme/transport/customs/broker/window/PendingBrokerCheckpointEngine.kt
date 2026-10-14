package com.acme.transport.customs.broker.window

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending broker checkpoint calculation. */
interface PendingBrokerCheckpointStep {
    fun describe(): String
    fun adjust(item: PendingBrokerCheckpoint, value: Double): Double
}

data class NamePendingBrokerCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingBrokerCheckpointStep {
    override fun describe(): String = "by name"

    override fun adjust(item: PendingBrokerCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredPendingBrokerCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingBrokerCheckpointStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingBrokerCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumPendingBrokerCheckpointAdjuster(var minimum: Double) : PendingBrokerCheckpointStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingBrokerCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentPendingBrokerCheckpointAdjuster(var percent: Double) : PendingBrokerCheckpointStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingBrokerCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapPendingBrokerCheckpointAdjuster(var cap: Double) : PendingBrokerCheckpointStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingBrokerCheckpoint, value: Double): Double = minOf(value, cap)
}

/** Runs the pending broker checkpoint steps in order and keeps a trace of each. */
data class PendingBrokerCheckpointEngine(
    var steps: MutableList<PendingBrokerCheckpointStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingBrokerCheckpoint): Double {
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

    fun evaluateAll(items: MutableList<PendingBrokerCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingBrokerCheckpointEngine(): PendingBrokerCheckpointEngine {
    return PendingBrokerCheckpointEngine(
        steps = mutableListOf<PendingBrokerCheckpointStep>(
            NamePendingBrokerCheckpointAdjuster(mutableMapOf<String, Double>("online" to 0.77, "south" to 1.39)),
            TieredPendingBrokerCheckpointAdjuster(
                mutableListOf<Double>(1425.0, 2501.0, 3292.0, 4665.0),
                mutableListOf<Double>(1.12, 0.99, 0.84, 1.02),
            ),
            MinimumPendingBrokerCheckpointAdjuster(48.2),
            PercentPendingBrokerCheckpointAdjuster(13.8),
            CapPendingBrokerCheckpointAdjuster(11158.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 54468.0,
    )
}
