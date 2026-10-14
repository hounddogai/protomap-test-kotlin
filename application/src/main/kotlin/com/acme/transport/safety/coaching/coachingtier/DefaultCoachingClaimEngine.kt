package com.acme.transport.safety.coaching.coachingtier

import com.acme.transport.kit.numbers.roundTo

/** One step of the default coaching claim calculation. */
interface DefaultCoachingClaimAdjuster {
    fun describe(): String
    fun adjust(item: DefaultCoachingClaim, value: Double): Double
}

data class TieredDefaultCoachingClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultCoachingClaimAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultCoachingClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DefaultCoachingClaimQuantityOffsetAdjuster(var weight: Double) : DefaultCoachingClaimAdjuster {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: DefaultCoachingClaim, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class MinimumDefaultCoachingClaimAdjuster(var minimum: Double) : DefaultCoachingClaimAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultCoachingClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingDefaultCoachingClaimAdjuster(var step: Double) : DefaultCoachingClaimAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultCoachingClaim, value: Double): Double = roundTo(value, step)
}

/** Runs the default coaching claim steps in order and keeps a trace of each. */
data class DefaultCoachingClaimEngine(
    var steps: MutableList<DefaultCoachingClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultCoachingClaim): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultCoachingClaimAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultCoachingClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultCoachingClaimEngine(): DefaultCoachingClaimEngine {
    return DefaultCoachingClaimEngine(
        steps = mutableListOf<DefaultCoachingClaimAdjuster>(
            TieredDefaultCoachingClaimAdjuster(
                mutableListOf<Double>(3524.0, 4213.0, 7395.0, 8575.0),
                mutableListOf<Double>(1.15, 1.12, 0.96, 0.83),
            ),
            DefaultCoachingClaimQuantityOffsetAdjuster(1.18),
            MinimumDefaultCoachingClaimAdjuster(37.8),
            RoundingDefaultCoachingClaimAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 65866.4,
    )
}
