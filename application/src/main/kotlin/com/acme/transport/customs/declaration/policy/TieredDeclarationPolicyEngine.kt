package com.acme.transport.customs.declaration.policy

import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered declaration policy calculation. */
interface TieredDeclarationPolicyFactor {
    fun describe(): String
    fun adjust(item: TieredDeclarationPolicy, value: Double): Double
}

data class PercentTieredDeclarationPolicyAdjuster(var percent: Double) : TieredDeclarationPolicyFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredDeclarationPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredTieredDeclarationPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredDeclarationPolicyFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredDeclarationPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapTieredDeclarationPolicyAdjuster(var cap: Double) : TieredDeclarationPolicyFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredDeclarationPolicy, value: Double): Double = minOf(value, cap)
}

data class RoundingTieredDeclarationPolicyAdjuster(var step: Double) : TieredDeclarationPolicyFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredDeclarationPolicy, value: Double): Double = roundTo(value, step)
}

data class MinimumTieredDeclarationPolicyAdjuster(var minimum: Double) : TieredDeclarationPolicyFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredDeclarationPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the tiered declaration policy steps in order and keeps a trace of each. */
data class TieredDeclarationPolicyEngine(
    var steps: MutableList<TieredDeclarationPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredDeclarationPolicy): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredDeclarationPolicyFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TieredDeclarationPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredDeclarationPolicyEngine(): TieredDeclarationPolicyEngine {
    return TieredDeclarationPolicyEngine(
        steps = mutableListOf<TieredDeclarationPolicyFactor>(
            PercentTieredDeclarationPolicyAdjuster(-13.4),
            TieredTieredDeclarationPolicyAdjuster(
                mutableListOf<Double>(6408.0, 6977.0),
                mutableListOf<Double>(1.08, 1.08),
            ),
            CapTieredDeclarationPolicyAdjuster(41080.0),
            RoundingTieredDeclarationPolicyAdjuster(0.5),
            MinimumTieredDeclarationPolicyAdjuster(26.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 80930.9,
    )
}
