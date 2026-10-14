package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the trailer yard profile calculation. */
interface TrailerYardProfileAdjuster {
    fun describe(): String
    fun adjust(item: TrailerYardProfile, value: Double): Double
}

data class PercentTrailerYardProfileAdjuster(var percent: Double) : TrailerYardProfileAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerYardProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapTrailerYardProfileAdjuster(var cap: Double) : TrailerYardProfileAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerYardProfile, value: Double): Double = minOf(value, cap)
}

data class TrailerYardProfileUpdatedAtOffsetAdjuster(var weight: Double) : TrailerYardProfileAdjuster {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: TrailerYardProfile, value: Double): Double = value + item.updatedAt.toDouble() * weight
}

data class MinimumTrailerYardProfileAdjuster(var minimum: Double) : TrailerYardProfileAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerYardProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredTrailerYardProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerYardProfileAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerYardProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the trailer yard profile steps in order and keeps a trace of each. */
data class TrailerYardProfileEngine(
    var steps: MutableList<TrailerYardProfileAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TrailerYardProfile): Double {
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

    fun evaluateAll(items: MutableList<TrailerYardProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTrailerYardProfileEngine(): TrailerYardProfileEngine {
    return TrailerYardProfileEngine(
        steps = mutableListOf<TrailerYardProfileAdjuster>(
            PercentTrailerYardProfileAdjuster(10.1),
            CapTrailerYardProfileAdjuster(32801.0),
            TrailerYardProfileUpdatedAtOffsetAdjuster(1.37),
            MinimumTrailerYardProfileAdjuster(13.3),
            TieredTrailerYardProfileAdjuster(
                mutableListOf<Double>(174.0, 5892.0, 7042.0, 7697.0),
                mutableListOf<Double>(1.13, 1.12, 1.09, 1.12),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 5184.9,
    )
}
