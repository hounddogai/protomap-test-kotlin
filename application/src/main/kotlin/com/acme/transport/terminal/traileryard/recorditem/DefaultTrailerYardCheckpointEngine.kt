package com.acme.transport.terminal.traileryard.recorditem

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default trailer yard checkpoint calculation. */
interface DefaultTrailerYardCheckpointFactor {
    fun describe(): String
    fun adjust(item: DefaultTrailerYardCheckpoint, value: Double): Double
}

data class TieredDefaultTrailerYardCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultTrailerYardCheckpointFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultTrailerYardCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DefaultTrailerYardCheckpointThresholdOffsetAdjuster(
    var weight: Double,
) : DefaultTrailerYardCheckpointFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: DefaultTrailerYardCheckpoint, value: Double): Double = value + item.threshold * weight
}

data class LocaleDefaultTrailerYardCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultTrailerYardCheckpointFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: DefaultTrailerYardCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapDefaultTrailerYardCheckpointAdjuster(var cap: Double) : DefaultTrailerYardCheckpointFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultTrailerYardCheckpoint, value: Double): Double = minOf(value, cap)
}

/** Runs the default trailer yard checkpoint steps in order and keeps a trace of each. */
data class DefaultTrailerYardCheckpointEngine(
    var steps: MutableList<DefaultTrailerYardCheckpointFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultTrailerYardCheckpoint): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultTrailerYardCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultTrailerYardCheckpointEngine(): DefaultTrailerYardCheckpointEngine {
    return DefaultTrailerYardCheckpointEngine(
        steps = mutableListOf<DefaultTrailerYardCheckpointFactor>(
            TieredDefaultTrailerYardCheckpointAdjuster(
                mutableListOf<Double>(291.0, 5462.0, 7224.0),
                mutableListOf<Double>(1.17, 0.82, 1.11),
            ),
            DefaultTrailerYardCheckpointThresholdOffsetAdjuster(0.69),
            LocaleDefaultTrailerYardCheckpointAdjuster(
                mutableMapOf<String, Double>("central" to 0.75, "online" to 1.17, "north" to 1.0),
            ),
            CapDefaultTrailerYardCheckpointAdjuster(37925.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 43306.7,
    )
}
