package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive near miss exception calculation. */
interface AdaptiveNearMissExceptionStep {
    fun describe(): String
    fun adjust(item: AdaptiveNearMissException, value: Double): Double
}

data class TieredAdaptiveNearMissExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveNearMissExceptionStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveNearMissException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentAdaptiveNearMissExceptionAdjuster(var percent: Double) : AdaptiveNearMissExceptionStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveNearMissException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CategoryAdaptiveNearMissExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveNearMissExceptionStep {
    override fun describe(): String = "by category"

    override fun adjust(item: AdaptiveNearMissException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the adaptive near miss exception steps in order and keeps a trace of each. */
data class AdaptiveNearMissExceptionEngine(
    var steps: MutableList<AdaptiveNearMissExceptionStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveNearMissException): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveNearMissException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveNearMissExceptionEngine(): AdaptiveNearMissExceptionEngine {
    return AdaptiveNearMissExceptionEngine(
        steps = mutableListOf<AdaptiveNearMissExceptionStep>(
            TieredAdaptiveNearMissExceptionAdjuster(
                mutableListOf<Double>(6643.0, 7260.0),
                mutableListOf<Double>(1.05, 0.81),
            ),
            PercentAdaptiveNearMissExceptionAdjuster(3.7),
            CategoryAdaptiveNearMissExceptionAdjuster(
                mutableMapOf<String, Double>("south" to 0.8, "retail" to 0.92, "west" to 1.36),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 61046.5,
    )
}
