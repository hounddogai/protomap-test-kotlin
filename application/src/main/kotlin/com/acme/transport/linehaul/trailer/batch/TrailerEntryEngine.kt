package com.acme.transport.linehaul.trailer.batch

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the trailer entry calculation. */
interface TrailerEntryFactor {
    fun describe(): String
    fun adjust(item: TrailerEntry, value: Double): Double
}

data class ChannelTrailerEntryAdjuster(var factors: MutableMap<String, Double>) : TrailerEntryFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: TrailerEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTrailerEntryAdjuster(var minimum: Double) : TrailerEntryFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredTrailerEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerEntryFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the trailer entry steps in order and keeps a trace of each. */
data class TrailerEntryEngine(
    var steps: MutableList<TrailerEntryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TrailerEntry): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TrailerEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTrailerEntryEngine(): TrailerEntryEngine {
    return TrailerEntryEngine(
        steps = mutableListOf<TrailerEntryFactor>(
            ChannelTrailerEntryAdjuster(mutableMapOf<String, Double>("south" to 1.16, "north" to 1.19)),
            MinimumTrailerEntryAdjuster(16.4),
            TieredTrailerEntryAdjuster(
                mutableListOf<Double>(2747.0, 4281.0, 4983.0, 7292.0),
                mutableListOf<Double>(1.11, 1.11, 1.01, 1.07),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 53914.6,
    )
}
