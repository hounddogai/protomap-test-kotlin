package com.acme.transport.linehaul.trailer.overrideitem

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal trailer override calculation. */
interface SeasonalTrailerOverrideStep {
    fun describe(): String
    fun adjust(item: SeasonalTrailerOverride, value: Double): Double
}

data class SeasonalTrailerOverrideRetriesOffsetAdjuster(var weight: Double) : SeasonalTrailerOverrideStep {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: SeasonalTrailerOverride, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class UnitSeasonalTrailerOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalTrailerOverrideStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: SeasonalTrailerOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapSeasonalTrailerOverrideAdjuster(var cap: Double) : SeasonalTrailerOverrideStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalTrailerOverride, value: Double): Double = minOf(value, cap)
}

data class RoundingSeasonalTrailerOverrideAdjuster(var step: Double) : SeasonalTrailerOverrideStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalTrailerOverride, value: Double): Double = roundTo(value, step)
}

data class MinimumSeasonalTrailerOverrideAdjuster(var minimum: Double) : SeasonalTrailerOverrideStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalTrailerOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredSeasonalTrailerOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalTrailerOverrideStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalTrailerOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the seasonal trailer override steps in order and keeps a trace of each. */
data class SeasonalTrailerOverrideEngine(
    var steps: MutableList<SeasonalTrailerOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalTrailerOverride): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalTrailerOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalTrailerOverrideEngine(): SeasonalTrailerOverrideEngine {
    return SeasonalTrailerOverrideEngine(
        steps = mutableListOf<SeasonalTrailerOverrideStep>(
            SeasonalTrailerOverrideRetriesOffsetAdjuster(0.67),
            UnitSeasonalTrailerOverrideAdjuster(mutableMapOf<String, Double>("east" to 1.39, "online" to 1.39)),
            CapSeasonalTrailerOverrideAdjuster(32221.0),
            RoundingSeasonalTrailerOverrideAdjuster(0.1),
            MinimumSeasonalTrailerOverrideAdjuster(36.7),
            TieredSeasonalTrailerOverrideAdjuster(
                mutableListOf<Double>(2488.0, 7778.0, 9090.0),
                mutableListOf<Double>(1.09, 1.14, 1.15),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 53628.7,
    )
}
