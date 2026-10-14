package com.acme.transport.emissions.fuelburn.milestone

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient fuel burn baseline calculation. */
interface LenientFuelBurnBaselineModifier {
    fun describe(): String
    fun adjust(item: LenientFuelBurnBaseline, value: Double): Double
}

data class LenientFuelBurnBaselineCostOffsetAdjuster(var weight: Double) : LenientFuelBurnBaselineModifier {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: LenientFuelBurnBaseline, value: Double): Double = value + item.cost * weight
}

data class RoundingLenientFuelBurnBaselineAdjuster(var step: Double) : LenientFuelBurnBaselineModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientFuelBurnBaseline, value: Double): Double = roundTo(value, step)
}

data class CapLenientFuelBurnBaselineAdjuster(var cap: Double) : LenientFuelBurnBaselineModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientFuelBurnBaseline, value: Double): Double = minOf(value, cap)
}

data class NameLenientFuelBurnBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientFuelBurnBaselineModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: LenientFuelBurnBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLenientFuelBurnBaselineAdjuster(var percent: Double) : LenientFuelBurnBaselineModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientFuelBurnBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumLenientFuelBurnBaselineAdjuster(var minimum: Double) : LenientFuelBurnBaselineModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientFuelBurnBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the lenient fuel burn baseline steps in order and keeps a trace of each. */
data class LenientFuelBurnBaselineEngine(
    var steps: MutableList<LenientFuelBurnBaselineModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientFuelBurnBaseline): Double {
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

    fun evaluateAll(items: MutableList<LenientFuelBurnBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientFuelBurnBaselineEngine(): LenientFuelBurnBaselineEngine {
    return LenientFuelBurnBaselineEngine(
        steps = mutableListOf<LenientFuelBurnBaselineModifier>(
            LenientFuelBurnBaselineCostOffsetAdjuster(1.2),
            RoundingLenientFuelBurnBaselineAdjuster(0.1),
            CapLenientFuelBurnBaselineAdjuster(44912.0),
            NameLenientFuelBurnBaselineAdjuster(
                mutableMapOf<String, Double>("central" to 1.19, "retail" to 0.88),
            ),
            PercentLenientFuelBurnBaselineAdjuster(16.2),
            MinimumLenientFuelBurnBaselineAdjuster(3.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 56708.2,
    )
}
