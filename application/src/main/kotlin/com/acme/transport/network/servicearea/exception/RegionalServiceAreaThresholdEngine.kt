package com.acme.transport.network.servicearea.exception

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional service area threshold calculation. */
interface RegionalServiceAreaThresholdFactor {
    fun describe(): String
    fun adjust(item: RegionalServiceAreaThreshold, value: Double): Double
}

data class RegionalServiceAreaThresholdScoreOffsetAdjuster(
    var weight: Double,
) : RegionalServiceAreaThresholdFactor {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: RegionalServiceAreaThreshold, value: Double): Double = value + item.score * weight
}

data class CapRegionalServiceAreaThresholdAdjuster(var cap: Double) : RegionalServiceAreaThresholdFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalServiceAreaThreshold, value: Double): Double = minOf(value, cap)
}

data class PercentRegionalServiceAreaThresholdAdjuster(
    var percent: Double,
) : RegionalServiceAreaThresholdFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalServiceAreaThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRegionalServiceAreaThresholdAdjuster(var step: Double) : RegionalServiceAreaThresholdFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalServiceAreaThreshold, value: Double): Double = roundTo(value, step)
}

data class MinimumRegionalServiceAreaThresholdAdjuster(
    var minimum: Double,
) : RegionalServiceAreaThresholdFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalServiceAreaThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRegionalServiceAreaThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalServiceAreaThresholdFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalServiceAreaThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the regional service area threshold steps in order and keeps a trace of each. */
data class RegionalServiceAreaThresholdEngine(
    var steps: MutableList<RegionalServiceAreaThresholdFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalServiceAreaThreshold): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalServiceAreaThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalServiceAreaThresholdEngine(): RegionalServiceAreaThresholdEngine {
    return RegionalServiceAreaThresholdEngine(
        steps = mutableListOf<RegionalServiceAreaThresholdFactor>(
            RegionalServiceAreaThresholdScoreOffsetAdjuster(0.05),
            CapRegionalServiceAreaThresholdAdjuster(38478.0),
            PercentRegionalServiceAreaThresholdAdjuster(-12.0),
            RoundingRegionalServiceAreaThresholdAdjuster(0.1),
            MinimumRegionalServiceAreaThresholdAdjuster(38.9),
            TieredRegionalServiceAreaThresholdAdjuster(
                mutableListOf<Double>(7051.0, 8523.0),
                mutableListOf<Double>(1.07, 0.81),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 77539.5,
    )
}
