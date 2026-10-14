package com.acme.transport.crewing.restbreak.claim

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback rest break checkpoint calculation. */
interface FallbackRestBreakCheckpointModifier {
    fun describe(): String
    fun adjust(item: FallbackRestBreakCheckpoint, value: Double): Double
}

data class LocaleFallbackRestBreakCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackRestBreakCheckpointModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: FallbackRestBreakCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapFallbackRestBreakCheckpointAdjuster(var cap: Double) : FallbackRestBreakCheckpointModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackRestBreakCheckpoint, value: Double): Double = minOf(value, cap)
}

data class MinimumFallbackRestBreakCheckpointAdjuster(
    var minimum: Double,
) : FallbackRestBreakCheckpointModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackRestBreakCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fallback rest break checkpoint steps in order and keeps a trace of each. */
data class FallbackRestBreakCheckpointEngine(
    var steps: MutableList<FallbackRestBreakCheckpointModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackRestBreakCheckpoint): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackRestBreakCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackRestBreakCheckpointEngine(): FallbackRestBreakCheckpointEngine {
    return FallbackRestBreakCheckpointEngine(
        steps = mutableListOf<FallbackRestBreakCheckpointModifier>(
            LocaleFallbackRestBreakCheckpointAdjuster(mutableMapOf<String, Double>("west" to 0.95)),
            CapFallbackRestBreakCheckpointAdjuster(35409.0),
            MinimumFallbackRestBreakCheckpointAdjuster(39.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 55071.1,
    )
}
