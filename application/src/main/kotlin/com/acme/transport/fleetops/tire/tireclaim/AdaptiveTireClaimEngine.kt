package com.acme.transport.fleetops.tire.tireclaim

import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive tire claim calculation. */
interface AdaptiveTireClaimStep {
    fun describe(): String
    fun adjust(item: AdaptiveTireClaim, value: Double): Double
}

data class RoundingAdaptiveTireClaimAdjuster(var step: Double) : AdaptiveTireClaimStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveTireClaim, value: Double): Double = roundTo(value, step)
}

data class PercentAdaptiveTireClaimAdjuster(var percent: Double) : AdaptiveTireClaimStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveTireClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumAdaptiveTireClaimAdjuster(var minimum: Double) : AdaptiveTireClaimStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveTireClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredAdaptiveTireClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveTireClaimStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveTireClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the adaptive tire claim steps in order and keeps a trace of each. */
data class AdaptiveTireClaimEngine(
    var steps: MutableList<AdaptiveTireClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveTireClaim): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveTireClaimStep = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveTireClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveTireClaimEngine(): AdaptiveTireClaimEngine {
    return AdaptiveTireClaimEngine(
        steps = mutableListOf<AdaptiveTireClaimStep>(
            RoundingAdaptiveTireClaimAdjuster(5.0),
            PercentAdaptiveTireClaimAdjuster(6.0),
            MinimumAdaptiveTireClaimAdjuster(1.7),
            TieredAdaptiveTireClaimAdjuster(
                mutableListOf<Double>(8202.0, 8209.0, 8898.0),
                mutableListOf<Double>(0.85, 0.95, 0.89),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 95658.0,
    )
}
