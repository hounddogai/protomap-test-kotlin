package com.acme.transport.lastmile.attempt.policy

import com.acme.transport.kit.flags.Registry

/** One step of the attempt policy calculation. */
interface AttemptPolicyFactor {
    fun describe(): String
    fun adjust(item: AttemptPolicy, value: Double): Double
}

data class CapAttemptPolicyAdjuster(var cap: Double) : AttemptPolicyFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AttemptPolicy, value: Double): Double = minOf(value, cap)
}

data class FlaggedAttemptPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AttemptPolicyFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AttemptPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class AttemptPolicyWindowDaysOffsetAdjuster(var weight: Double) : AttemptPolicyFactor {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: AttemptPolicy, value: Double): Double = value + item.windowDays.toDouble() * weight
}

data class CategoryAttemptPolicyAdjuster(var factors: MutableMap<String, Double>) : AttemptPolicyFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: AttemptPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentAttemptPolicyAdjuster(var percent: Double) : AttemptPolicyFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AttemptPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the attempt policy steps in order and keeps a trace of each. */
data class AttemptPolicyEngine(
    var steps: MutableList<AttemptPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AttemptPolicy): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AttemptPolicyFactor = steps[index]
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

    fun evaluateAll(items: MutableList<AttemptPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAttemptPolicyEngine(flags: Registry): AttemptPolicyEngine {
    return AttemptPolicyEngine(
        steps = mutableListOf<AttemptPolicyFactor>(
            CapAttemptPolicyAdjuster(12899.0),
            FlaggedAttemptPolicyAdjuster(flags, "attempt-policy-legacy", -1.6),
            AttemptPolicyWindowDaysOffsetAdjuster(1.37),
            CategoryAttemptPolicyAdjuster(mutableMapOf<String, Double>("south" to 1.09)),
            PercentAttemptPolicyAdjuster(-9.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 81856.1,
    )
}
