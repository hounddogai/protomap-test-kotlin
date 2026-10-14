package com.acme.transport.fleetops.tire.request

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive tire exception calculation. */
interface AdaptiveTireExceptionModifier {
    fun describe(): String
    fun adjust(item: AdaptiveTireException, value: Double): Double
}

data class MinimumAdaptiveTireExceptionAdjuster(var minimum: Double) : AdaptiveTireExceptionModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveTireException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredAdaptiveTireExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveTireExceptionModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveTireException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapAdaptiveTireExceptionAdjuster(var cap: Double) : AdaptiveTireExceptionModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveTireException, value: Double): Double = minOf(value, cap)
}

data class AdaptiveTireExceptionBatchSizeOffsetAdjuster(var weight: Double) : AdaptiveTireExceptionModifier {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: AdaptiveTireException, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

/** Runs the adaptive tire exception steps in order and keeps a trace of each. */
data class AdaptiveTireExceptionEngine(
    var steps: MutableList<AdaptiveTireExceptionModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveTireException): Double {
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

    fun evaluateAll(items: MutableList<AdaptiveTireException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveTireExceptionEngine(): AdaptiveTireExceptionEngine {
    return AdaptiveTireExceptionEngine(
        steps = mutableListOf<AdaptiveTireExceptionModifier>(
            MinimumAdaptiveTireExceptionAdjuster(37.9),
            TieredAdaptiveTireExceptionAdjuster(
                mutableListOf<Double>(206.0, 3586.0, 6505.0),
                mutableListOf<Double>(0.87, 1.07, 1.03),
            ),
            CapAdaptiveTireExceptionAdjuster(22597.0),
            AdaptiveTireExceptionBatchSizeOffsetAdjuster(1.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 4873.4,
    )
}
