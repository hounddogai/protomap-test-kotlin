package com.acme.transport.terminal.traileryard.schedule

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the trailer yard rollup calculation. */
interface TrailerYardRollupModifier {
    fun describe(): String
    fun adjust(item: TrailerYardRollup, value: Double): Double
}

data class RoundingTrailerYardRollupAdjuster(var step: Double) : TrailerYardRollupModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerYardRollup, value: Double): Double = roundTo(value, step)
}

data class PercentTrailerYardRollupAdjuster(var percent: Double) : TrailerYardRollupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerYardRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredTrailerYardRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerYardRollupModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerYardRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapTrailerYardRollupAdjuster(var cap: Double) : TrailerYardRollupModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerYardRollup, value: Double): Double = minOf(value, cap)
}

data class TrailerYardRollupUpdatedAtOffsetAdjuster(var weight: Double) : TrailerYardRollupModifier {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: TrailerYardRollup, value: Double): Double = value + item.updatedAt.toDouble() * weight
}

data class MinimumTrailerYardRollupAdjuster(var minimum: Double) : TrailerYardRollupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerYardRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the trailer yard rollup steps in order and keeps a trace of each. */
data class TrailerYardRollupEngine(
    var steps: MutableList<TrailerYardRollupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TrailerYardRollup): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TrailerYardRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTrailerYardRollupEngine(): TrailerYardRollupEngine {
    return TrailerYardRollupEngine(
        steps = mutableListOf<TrailerYardRollupModifier>(
            RoundingTrailerYardRollupAdjuster(0.25),
            PercentTrailerYardRollupAdjuster(-10.5),
            TieredTrailerYardRollupAdjuster(
                mutableListOf<Double>(7531.0, 8985.0),
                mutableListOf<Double>(0.94, 1.06),
            ),
            CapTrailerYardRollupAdjuster(37012.0),
            TrailerYardRollupUpdatedAtOffsetAdjuster(0.35),
            MinimumTrailerYardRollupAdjuster(13.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 70238.0,
    )
}
