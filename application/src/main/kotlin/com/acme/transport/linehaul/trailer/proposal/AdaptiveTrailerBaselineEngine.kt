package com.acme.transport.linehaul.trailer.proposal

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive trailer baseline calculation. */
interface AdaptiveTrailerBaselineModifier {
    fun describe(): String
    fun adjust(item: AdaptiveTrailerBaseline, value: Double): Double
}

data class PercentAdaptiveTrailerBaselineAdjuster(var percent: Double) : AdaptiveTrailerBaselineModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveTrailerBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAdaptiveTrailerBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveTrailerBaselineModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveTrailerBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameAdaptiveTrailerBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveTrailerBaselineModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: AdaptiveTrailerBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class AdaptiveTrailerBaselineDiscountOffsetAdjuster(var weight: Double) : AdaptiveTrailerBaselineModifier {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: AdaptiveTrailerBaseline, value: Double): Double = value + item.discount * weight
}

data class CapAdaptiveTrailerBaselineAdjuster(var cap: Double) : AdaptiveTrailerBaselineModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveTrailerBaseline, value: Double): Double = minOf(value, cap)
}

data class RoundingAdaptiveTrailerBaselineAdjuster(var step: Double) : AdaptiveTrailerBaselineModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveTrailerBaseline, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive trailer baseline steps in order and keeps a trace of each. */
data class AdaptiveTrailerBaselineEngine(
    var steps: MutableList<AdaptiveTrailerBaselineModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveTrailerBaseline): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveTrailerBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveTrailerBaselineEngine(): AdaptiveTrailerBaselineEngine {
    return AdaptiveTrailerBaselineEngine(
        steps = mutableListOf<AdaptiveTrailerBaselineModifier>(
            PercentAdaptiveTrailerBaselineAdjuster(-19.8),
            TieredAdaptiveTrailerBaselineAdjuster(
                mutableListOf<Double>(1599.0, 5068.0, 6452.0, 9594.0),
                mutableListOf<Double>(1.06, 0.85, 1.02, 1.19),
            ),
            NameAdaptiveTrailerBaselineAdjuster(mutableMapOf<String, Double>("north" to 1.06)),
            AdaptiveTrailerBaselineDiscountOffsetAdjuster(1.65),
            CapAdaptiveTrailerBaselineAdjuster(47291.0),
            RoundingAdaptiveTrailerBaselineAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 65127.0,
    )
}
