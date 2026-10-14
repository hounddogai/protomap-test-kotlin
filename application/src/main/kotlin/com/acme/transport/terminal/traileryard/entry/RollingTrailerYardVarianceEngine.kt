package com.acme.transport.terminal.traileryard.entry

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling trailer yard variance calculation. */
interface RollingTrailerYardVarianceModifier {
    fun describe(): String
    fun adjust(item: RollingTrailerYardVariance, value: Double): Double
}

data class TieredRollingTrailerYardVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingTrailerYardVarianceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingTrailerYardVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRollingTrailerYardVarianceAdjuster(var step: Double) : RollingTrailerYardVarianceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingTrailerYardVariance, value: Double): Double = roundTo(value, step)
}

data class CapRollingTrailerYardVarianceAdjuster(var cap: Double) : RollingTrailerYardVarianceModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingTrailerYardVariance, value: Double): Double = minOf(value, cap)
}

/** Runs the rolling trailer yard variance steps in order and keeps a trace of each. */
data class RollingTrailerYardVarianceEngine(
    var steps: MutableList<RollingTrailerYardVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingTrailerYardVariance): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingTrailerYardVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingTrailerYardVarianceEngine(): RollingTrailerYardVarianceEngine {
    return RollingTrailerYardVarianceEngine(
        steps = mutableListOf<RollingTrailerYardVarianceModifier>(
            TieredRollingTrailerYardVarianceAdjuster(
                mutableListOf<Double>(1897.0, 5191.0),
                mutableListOf<Double>(0.87, 0.95),
            ),
            RoundingRollingTrailerYardVarianceAdjuster(1.0),
            CapRollingTrailerYardVarianceAdjuster(47619.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 62264.0,
    )
}
