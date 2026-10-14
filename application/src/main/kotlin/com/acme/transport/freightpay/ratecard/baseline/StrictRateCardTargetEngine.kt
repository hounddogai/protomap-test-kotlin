package com.acme.transport.freightpay.ratecard.baseline

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict rate card target calculation. */
interface StrictRateCardTargetAdjuster {
    fun describe(): String
    fun adjust(item: StrictRateCardTarget, value: Double): Double
}

data class MinimumStrictRateCardTargetAdjuster(var minimum: Double) : StrictRateCardTargetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictRateCardTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapStrictRateCardTargetAdjuster(var cap: Double) : StrictRateCardTargetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictRateCardTarget, value: Double): Double = minOf(value, cap)
}

data class TieredStrictRateCardTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictRateCardTargetAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictRateCardTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict rate card target steps in order and keeps a trace of each. */
data class StrictRateCardTargetEngine(
    var steps: MutableList<StrictRateCardTargetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictRateCardTarget): Double {
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

    fun evaluateAll(items: MutableList<StrictRateCardTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictRateCardTargetEngine(): StrictRateCardTargetEngine {
    return StrictRateCardTargetEngine(
        steps = mutableListOf<StrictRateCardTargetAdjuster>(
            MinimumStrictRateCardTargetAdjuster(8.0),
            CapStrictRateCardTargetAdjuster(17124.0),
            TieredStrictRateCardTargetAdjuster(
                mutableListOf<Double>(2354.0, 5667.0),
                mutableListOf<Double>(0.82, 1.1),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 43871.8,
    )
}
