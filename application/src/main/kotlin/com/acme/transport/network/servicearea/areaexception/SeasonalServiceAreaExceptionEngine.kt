package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal service area exception calculation. */
interface SeasonalServiceAreaExceptionModifier {
    fun describe(): String
    fun adjust(item: SeasonalServiceAreaException, value: Double): Double
}

data class CapSeasonalServiceAreaExceptionAdjuster(var cap: Double) : SeasonalServiceAreaExceptionModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalServiceAreaException, value: Double): Double = minOf(value, cap)
}

data class RoundingSeasonalServiceAreaExceptionAdjuster(
    var step: Double,
) : SeasonalServiceAreaExceptionModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalServiceAreaException, value: Double): Double = roundTo(value, step)
}

data class PercentSeasonalServiceAreaExceptionAdjuster(
    var percent: Double,
) : SeasonalServiceAreaExceptionModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalServiceAreaException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredSeasonalServiceAreaExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalServiceAreaExceptionModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalServiceAreaException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the seasonal service area exception steps in order and keeps a trace of each. */
data class SeasonalServiceAreaExceptionEngine(
    var steps: MutableList<SeasonalServiceAreaExceptionModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalServiceAreaException): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalServiceAreaException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalServiceAreaExceptionEngine(): SeasonalServiceAreaExceptionEngine {
    return SeasonalServiceAreaExceptionEngine(
        steps = mutableListOf<SeasonalServiceAreaExceptionModifier>(
            CapSeasonalServiceAreaExceptionAdjuster(4209.0),
            RoundingSeasonalServiceAreaExceptionAdjuster(0.5),
            PercentSeasonalServiceAreaExceptionAdjuster(-10.6),
            TieredSeasonalServiceAreaExceptionAdjuster(
                mutableListOf<Double>(4460.0, 4733.0),
                mutableListOf<Double>(1.0, 0.81),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 19800.6,
    )
}
