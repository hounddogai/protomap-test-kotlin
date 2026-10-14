package com.acme.transport.network.cutoff.globalcutoffledger

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback cutoff claim calculation. */
interface FallbackCutoffClaimStep {
    fun describe(): String
    fun adjust(item: FallbackCutoffClaim, value: Double): Double
}

data class PercentFallbackCutoffClaimAdjuster(var percent: Double) : FallbackCutoffClaimStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackCutoffClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapFallbackCutoffClaimAdjuster(var cap: Double) : FallbackCutoffClaimStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackCutoffClaim, value: Double): Double = minOf(value, cap)
}

data class TieredFallbackCutoffClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackCutoffClaimStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackCutoffClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingFallbackCutoffClaimAdjuster(var step: Double) : FallbackCutoffClaimStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackCutoffClaim, value: Double): Double = roundTo(value, step)
}

/** Runs the fallback cutoff claim steps in order and keeps a trace of each. */
data class FallbackCutoffClaimEngine(
    var steps: MutableList<FallbackCutoffClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackCutoffClaim): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackCutoffClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackCutoffClaimEngine(): FallbackCutoffClaimEngine {
    return FallbackCutoffClaimEngine(
        steps = mutableListOf<FallbackCutoffClaimStep>(
            PercentFallbackCutoffClaimAdjuster(-24.9),
            CapFallbackCutoffClaimAdjuster(20386.0),
            TieredFallbackCutoffClaimAdjuster(
                mutableListOf<Double>(2055.0, 8975.0, 9710.0),
                mutableListOf<Double>(1.2, 1.03, 1.16),
            ),
            RoundingFallbackCutoffClaimAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 72804.9,
    )
}
