package com.acme.transport.linehaul.trailer.template

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the trailer threshold calculation. */
interface TrailerThresholdFactor {
    fun describe(): String
    fun adjust(item: TrailerThreshold, value: Double): Double
}

data class PercentTrailerThresholdAdjuster(var percent: Double) : TrailerThresholdFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumTrailerThresholdAdjuster(var minimum: Double) : TrailerThresholdFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapTrailerThresholdAdjuster(var cap: Double) : TrailerThresholdFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerThreshold, value: Double): Double = minOf(value, cap)
}

data class TieredTrailerThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerThresholdFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the trailer threshold steps in order and keeps a trace of each. */
data class TrailerThresholdEngine(
    var steps: MutableList<TrailerThresholdFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TrailerThreshold): Double {
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

    fun evaluateAll(items: MutableList<TrailerThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTrailerThresholdEngine(): TrailerThresholdEngine {
    return TrailerThresholdEngine(
        steps = mutableListOf<TrailerThresholdFactor>(
            PercentTrailerThresholdAdjuster(-6.5),
            MinimumTrailerThresholdAdjuster(47.2),
            CapTrailerThresholdAdjuster(16154.0),
            TieredTrailerThresholdAdjuster(
                mutableListOf<Double>(806.0, 1067.0, 5182.0),
                mutableListOf<Double>(1.19, 0.82, 0.93),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 65693.6,
    )
}
