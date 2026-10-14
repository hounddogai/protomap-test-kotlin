package com.acme.transport.crewing.swap.policy

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive swap policy calculation. */
interface AdaptiveSwapPolicyModifier {
    fun describe(): String
    fun adjust(item: AdaptiveSwapPolicy, value: Double): Double
}

data class CapAdaptiveSwapPolicyAdjuster(var cap: Double) : AdaptiveSwapPolicyModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveSwapPolicy, value: Double): Double = minOf(value, cap)
}

data class TieredAdaptiveSwapPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveSwapPolicyModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveSwapPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class AdaptiveSwapPolicyEtaMinutesOffsetAdjuster(var weight: Double) : AdaptiveSwapPolicyModifier {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: AdaptiveSwapPolicy, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

/** Runs the adaptive swap policy steps in order and keeps a trace of each. */
data class AdaptiveSwapPolicyEngine(
    var steps: MutableList<AdaptiveSwapPolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveSwapPolicy): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveSwapPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveSwapPolicyEngine(): AdaptiveSwapPolicyEngine {
    return AdaptiveSwapPolicyEngine(
        steps = mutableListOf<AdaptiveSwapPolicyModifier>(
            CapAdaptiveSwapPolicyAdjuster(38363.0),
            TieredAdaptiveSwapPolicyAdjuster(
                mutableListOf<Double>(6063.0, 9291.0),
                mutableListOf<Double>(1.08, 0.87),
            ),
            AdaptiveSwapPolicyEtaMinutesOffsetAdjuster(1.92),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 82559.6,
    )
}
