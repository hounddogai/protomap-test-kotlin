package com.acme.transport.terminal.dwell.assignment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback dwell preference calculation. */
interface FallbackDwellPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: FallbackDwellPreference, value: Double): Double
}

data class CapFallbackDwellPreferenceAdjuster(var cap: Double) : FallbackDwellPreferenceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackDwellPreference, value: Double): Double = minOf(value, cap)
}

data class FallbackDwellPreferenceDiscountOffsetAdjuster(var weight: Double) : FallbackDwellPreferenceAdjuster {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: FallbackDwellPreference, value: Double): Double = value + item.discount * weight
}

data class PercentFallbackDwellPreferenceAdjuster(var percent: Double) : FallbackDwellPreferenceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackDwellPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumFallbackDwellPreferenceAdjuster(var minimum: Double) : FallbackDwellPreferenceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackDwellPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingFallbackDwellPreferenceAdjuster(var step: Double) : FallbackDwellPreferenceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackDwellPreference, value: Double): Double = roundTo(value, step)
}

/** Runs the fallback dwell preference steps in order and keeps a trace of each. */
data class FallbackDwellPreferenceEngine(
    var steps: MutableList<FallbackDwellPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackDwellPreference): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackDwellPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackDwellPreferenceEngine(): FallbackDwellPreferenceEngine {
    return FallbackDwellPreferenceEngine(
        steps = mutableListOf<FallbackDwellPreferenceAdjuster>(
            CapFallbackDwellPreferenceAdjuster(32600.0),
            FallbackDwellPreferenceDiscountOffsetAdjuster(0.38),
            PercentFallbackDwellPreferenceAdjuster(21.2),
            MinimumFallbackDwellPreferenceAdjuster(38.4),
            RoundingFallbackDwellPreferenceAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 80860.2,
    )
}
