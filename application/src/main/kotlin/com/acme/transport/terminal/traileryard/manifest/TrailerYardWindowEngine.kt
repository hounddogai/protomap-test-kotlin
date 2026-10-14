package com.acme.transport.terminal.traileryard.manifest

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the trailer yard window calculation. */
interface TrailerYardWindowModifier {
    fun describe(): String
    fun adjust(item: TrailerYardWindow, value: Double): Double
}

data class CapTrailerYardWindowAdjuster(var cap: Double) : TrailerYardWindowModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerYardWindow, value: Double): Double = minOf(value, cap)
}

data class UnitTrailerYardWindowAdjuster(var factors: MutableMap<String, Double>) : TrailerYardWindowModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: TrailerYardWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredTrailerYardWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerYardWindowModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerYardWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumTrailerYardWindowAdjuster(var minimum: Double) : TrailerYardWindowModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerYardWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the trailer yard window steps in order and keeps a trace of each. */
data class TrailerYardWindowEngine(
    var steps: MutableList<TrailerYardWindowModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TrailerYardWindow): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TrailerYardWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTrailerYardWindowEngine(): TrailerYardWindowEngine {
    return TrailerYardWindowEngine(
        steps = mutableListOf<TrailerYardWindowModifier>(
            CapTrailerYardWindowAdjuster(22830.0),
            UnitTrailerYardWindowAdjuster(
                mutableMapOf<String, Double>("west" to 1.31, "south" to 0.95, "online" to 1.34),
            ),
            TieredTrailerYardWindowAdjuster(
                mutableListOf<Double>(3277.0, 9961.0),
                mutableListOf<Double>(0.87, 1.01),
            ),
            MinimumTrailerYardWindowAdjuster(39.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 69070.3,
    )
}
