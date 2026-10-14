package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.kit.flags.Registry

/** One step of the rest break policy calculation. */
interface RestBreakPolicyStep {
    fun describe(): String
    fun adjust(item: RestBreakPolicy, value: Double): Double
}

data class CapRestBreakPolicyAdjuster(var cap: Double) : RestBreakPolicyStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RestBreakPolicy, value: Double): Double = minOf(value, cap)
}

data class MinimumRestBreakPolicyAdjuster(var minimum: Double) : RestBreakPolicyStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RestBreakPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRestBreakPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RestBreakPolicyStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RestBreakPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RestBreakPolicyCreatedAtOffsetAdjuster(var weight: Double) : RestBreakPolicyStep {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: RestBreakPolicy, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class TieredRestBreakPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RestBreakPolicyStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RestBreakPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRestBreakPolicyAdjuster(var percent: Double) : RestBreakPolicyStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RestBreakPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the rest break policy steps in order and keeps a trace of each. */
data class RestBreakPolicyEngine(
    var steps: MutableList<RestBreakPolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RestBreakPolicy): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RestBreakPolicyStep = steps[index]
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

    fun evaluateAll(items: MutableList<RestBreakPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRestBreakPolicyEngine(flags: Registry): RestBreakPolicyEngine {
    return RestBreakPolicyEngine(
        steps = mutableListOf<RestBreakPolicyStep>(
            CapRestBreakPolicyAdjuster(13244.0),
            MinimumRestBreakPolicyAdjuster(11.1),
            FlaggedRestBreakPolicyAdjuster(flags, "rest-break-policy-lenient", -2.9),
            RestBreakPolicyCreatedAtOffsetAdjuster(1.3),
            TieredRestBreakPolicyAdjuster(
                mutableListOf<Double>(1732.0, 3660.0),
                mutableListOf<Double>(0.99, 0.99),
            ),
            PercentRestBreakPolicyAdjuster(20.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 39142.2,
    )
}
