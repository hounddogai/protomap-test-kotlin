package com.acme.transport.safety.nearmiss.missclaim

import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive near miss claim calculation. */
interface AdaptiveNearMissClaimStep {
    fun describe(): String
    fun adjust(item: AdaptiveNearMissClaim, value: Double): Double
}

data class ChannelAdaptiveNearMissClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveNearMissClaimStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: AdaptiveNearMissClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAdaptiveNearMissClaimAdjuster(var minimum: Double) : AdaptiveNearMissClaimStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveNearMissClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredAdaptiveNearMissClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveNearMissClaimStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveNearMissClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingAdaptiveNearMissClaimAdjuster(var step: Double) : AdaptiveNearMissClaimStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveNearMissClaim, value: Double): Double = roundTo(value, step)
}

data class PercentAdaptiveNearMissClaimAdjuster(var percent: Double) : AdaptiveNearMissClaimStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveNearMissClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the adaptive near miss claim steps in order and keeps a trace of each. */
data class AdaptiveNearMissClaimEngine(
    var steps: MutableList<AdaptiveNearMissClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveNearMissClaim): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveNearMissClaimStep = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveNearMissClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveNearMissClaimEngine(): AdaptiveNearMissClaimEngine {
    return AdaptiveNearMissClaimEngine(
        steps = mutableListOf<AdaptiveNearMissClaimStep>(
            ChannelAdaptiveNearMissClaimAdjuster(mutableMapOf<String, Double>("retail" to 0.75)),
            MinimumAdaptiveNearMissClaimAdjuster(38.3),
            TieredAdaptiveNearMissClaimAdjuster(
                mutableListOf<Double>(402.0, 7222.0, 7762.0),
                mutableListOf<Double>(1.14, 1.01, 0.88),
            ),
            RoundingAdaptiveNearMissClaimAdjuster(1.0),
            PercentAdaptiveNearMissClaimAdjuster(0.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 18447.9,
    )
}
