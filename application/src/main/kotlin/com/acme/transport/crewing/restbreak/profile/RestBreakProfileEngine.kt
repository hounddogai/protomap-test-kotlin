package com.acme.transport.crewing.restbreak.profile

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rest break profile calculation. */
interface RestBreakProfileAdjuster {
    fun describe(): String
    fun adjust(item: RestBreakProfile, value: Double): Double
}

data class RestBreakProfileStopCountOffsetAdjuster(var weight: Double) : RestBreakProfileAdjuster {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: RestBreakProfile, value: Double): Double = value + item.stopCount.toDouble() * weight
}

data class TieredRestBreakProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RestBreakProfileAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RestBreakProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRestBreakProfileAdjuster(var step: Double) : RestBreakProfileAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RestBreakProfile, value: Double): Double = roundTo(value, step)
}

/** Runs the rest break profile steps in order and keeps a trace of each. */
data class RestBreakProfileEngine(
    var steps: MutableList<RestBreakProfileAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RestBreakProfile): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RestBreakProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRestBreakProfileEngine(): RestBreakProfileEngine {
    return RestBreakProfileEngine(
        steps = mutableListOf<RestBreakProfileAdjuster>(
            RestBreakProfileStopCountOffsetAdjuster(0.94),
            TieredRestBreakProfileAdjuster(
                mutableListOf<Double>(8220.0, 9181.0),
                mutableListOf<Double>(0.86, 0.87),
            ),
            RoundingRestBreakProfileAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 77298.9,
    )
}
