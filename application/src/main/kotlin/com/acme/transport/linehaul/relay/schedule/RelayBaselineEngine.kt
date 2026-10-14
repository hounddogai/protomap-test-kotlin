package com.acme.transport.linehaul.relay.schedule

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the relay baseline calculation. */
interface RelayBaselineFactor {
    fun describe(): String
    fun adjust(item: RelayBaseline, value: Double): Double
}

data class TitleRelayBaselineAdjuster(var factors: MutableMap<String, Double>) : RelayBaselineFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: RelayBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRelayBaselineAdjuster(var minimum: Double) : RelayBaselineFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RelayBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRelayBaselineAdjuster(var percent: Double) : RelayBaselineFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RelayBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the relay baseline steps in order and keeps a trace of each. */
data class RelayBaselineEngine(
    var steps: MutableList<RelayBaselineFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RelayBaseline): Double {
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

    fun evaluateAll(items: MutableList<RelayBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRelayBaselineEngine(): RelayBaselineEngine {
    return RelayBaselineEngine(
        steps = mutableListOf<RelayBaselineFactor>(
            TitleRelayBaselineAdjuster(
                mutableMapOf<String, Double>("south" to 1.19, "east" to 1.36, "north" to 1.34),
            ),
            MinimumRelayBaselineAdjuster(13.4),
            PercentRelayBaselineAdjuster(8.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 5736.1,
    )
}
