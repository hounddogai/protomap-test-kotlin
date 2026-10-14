package com.acme.transport.crewing.restbreak.breakprofile

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rest break checkpoint calculation. */
interface RestBreakCheckpointFactor {
    fun describe(): String
    fun adjust(item: RestBreakCheckpoint, value: Double): Double
}

data class LaneRestBreakCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : RestBreakCheckpointFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: RestBreakCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRestBreakCheckpointAdjuster(var minimum: Double) : RestBreakCheckpointFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RestBreakCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRestBreakCheckpointAdjuster(var step: Double) : RestBreakCheckpointFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RestBreakCheckpoint, value: Double): Double = roundTo(value, step)
}

/** Runs the rest break checkpoint steps in order and keeps a trace of each. */
data class RestBreakCheckpointEngine(
    var steps: MutableList<RestBreakCheckpointFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RestBreakCheckpoint): Double {
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

    fun evaluateAll(items: MutableList<RestBreakCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRestBreakCheckpointEngine(): RestBreakCheckpointEngine {
    return RestBreakCheckpointEngine(
        steps = mutableListOf<RestBreakCheckpointFactor>(
            LaneRestBreakCheckpointAdjuster(mutableMapOf<String, Double>("retail" to 1.22, "west" to 0.96)),
            MinimumRestBreakCheckpointAdjuster(41.8),
            RoundingRestBreakCheckpointAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 57992.7,
    )
}
