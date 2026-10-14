package com.acme.transport.terminal.crossdock.baseline

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient cross dock baseline calculation. */
interface LenientCrossDockBaselineModifier {
    fun describe(): String
    fun adjust(item: LenientCrossDockBaseline, value: Double): Double
}

data class RoundingLenientCrossDockBaselineAdjuster(var step: Double) : LenientCrossDockBaselineModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientCrossDockBaseline, value: Double): Double = roundTo(value, step)
}

data class TieredLenientCrossDockBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientCrossDockBaselineModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientCrossDockBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapLenientCrossDockBaselineAdjuster(var cap: Double) : LenientCrossDockBaselineModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientCrossDockBaseline, value: Double): Double = minOf(value, cap)
}

data class LenientCrossDockBaselineMarginOffsetAdjuster(var weight: Double) : LenientCrossDockBaselineModifier {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: LenientCrossDockBaseline, value: Double): Double = value + item.margin * weight
}

data class MinimumLenientCrossDockBaselineAdjuster(var minimum: Double) : LenientCrossDockBaselineModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientCrossDockBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the lenient cross dock baseline steps in order and keeps a trace of each. */
data class LenientCrossDockBaselineEngine(
    var steps: MutableList<LenientCrossDockBaselineModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientCrossDockBaseline): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientCrossDockBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientCrossDockBaselineEngine(): LenientCrossDockBaselineEngine {
    return LenientCrossDockBaselineEngine(
        steps = mutableListOf<LenientCrossDockBaselineModifier>(
            RoundingLenientCrossDockBaselineAdjuster(0.05),
            TieredLenientCrossDockBaselineAdjuster(
                mutableListOf<Double>(184.0, 2010.0, 8867.0),
                mutableListOf<Double>(0.92, 1.05, 0.92),
            ),
            CapLenientCrossDockBaselineAdjuster(12190.0),
            LenientCrossDockBaselineMarginOffsetAdjuster(0.07),
            MinimumLenientCrossDockBaselineAdjuster(10.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 12468.9,
    )
}
