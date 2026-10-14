package com.acme.transport.safety.nearmiss.policy

import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient near miss policy calculation. */
interface LenientNearMissPolicyFactor {
    fun describe(): String
    fun adjust(item: LenientNearMissPolicy, value: Double): Double
}

data class RoundingLenientNearMissPolicyAdjuster(var step: Double) : LenientNearMissPolicyFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientNearMissPolicy, value: Double): Double = roundTo(value, step)
}

data class MinimumLenientNearMissPolicyAdjuster(var minimum: Double) : LenientNearMissPolicyFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientNearMissPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredLenientNearMissPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientNearMissPolicyFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientNearMissPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LenientNearMissPolicyBudgetOffsetAdjuster(var weight: Double) : LenientNearMissPolicyFactor {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: LenientNearMissPolicy, value: Double): Double = value + item.budget * weight
}

data class CapLenientNearMissPolicyAdjuster(var cap: Double) : LenientNearMissPolicyFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientNearMissPolicy, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient near miss policy steps in order and keeps a trace of each. */
data class LenientNearMissPolicyEngine(
    var steps: MutableList<LenientNearMissPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientNearMissPolicy): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientNearMissPolicyFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LenientNearMissPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientNearMissPolicyEngine(): LenientNearMissPolicyEngine {
    return LenientNearMissPolicyEngine(
        steps = mutableListOf<LenientNearMissPolicyFactor>(
            RoundingLenientNearMissPolicyAdjuster(0.1),
            MinimumLenientNearMissPolicyAdjuster(45.6),
            TieredLenientNearMissPolicyAdjuster(
                mutableListOf<Double>(7038.0, 9510.0),
                mutableListOf<Double>(1.0, 0.83),
            ),
            LenientNearMissPolicyBudgetOffsetAdjuster(1.64),
            CapLenientNearMissPolicyAdjuster(24696.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 70206.2,
    )
}
