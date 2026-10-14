package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary idling checkpoint calculation. */
interface PrimaryIdlingCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryIdlingCheckpoint, value: Double): Double
}

data class PrimaryIdlingCheckpointDiscountOffsetAdjuster(var weight: Double) : PrimaryIdlingCheckpointAdjuster {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: PrimaryIdlingCheckpoint, value: Double): Double = value + item.discount * weight
}

data class MinimumPrimaryIdlingCheckpointAdjuster(var minimum: Double) : PrimaryIdlingCheckpointAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryIdlingCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentPrimaryIdlingCheckpointAdjuster(var percent: Double) : PrimaryIdlingCheckpointAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryIdlingCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the primary idling checkpoint steps in order and keeps a trace of each. */
data class PrimaryIdlingCheckpointEngine(
    var steps: MutableList<PrimaryIdlingCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryIdlingCheckpoint): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryIdlingCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryIdlingCheckpointEngine(): PrimaryIdlingCheckpointEngine {
    return PrimaryIdlingCheckpointEngine(
        steps = mutableListOf<PrimaryIdlingCheckpointAdjuster>(
            PrimaryIdlingCheckpointDiscountOffsetAdjuster(1.21),
            MinimumPrimaryIdlingCheckpointAdjuster(41.4),
            PercentPrimaryIdlingCheckpointAdjuster(-24.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 46899.0,
    )
}
