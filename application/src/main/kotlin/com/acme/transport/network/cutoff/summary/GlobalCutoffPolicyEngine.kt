package com.acme.transport.network.cutoff.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global cutoff policy calculation. */
interface GlobalCutoffPolicyStep {
    fun describe(): String
    fun adjust(item: GlobalCutoffPolicy, value: Double): Double
}

data class RoundingGlobalCutoffPolicyAdjuster(var step: Double) : GlobalCutoffPolicyStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalCutoffPolicy, value: Double): Double = roundTo(value, step)
}

data class MinimumGlobalCutoffPolicyAdjuster(var minimum: Double) : GlobalCutoffPolicyStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalCutoffPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentGlobalCutoffPolicyAdjuster(var percent: Double) : GlobalCutoffPolicyStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalCutoffPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SegmentGlobalCutoffPolicyAdjuster(var factors: MutableMap<String, Double>) : GlobalCutoffPolicyStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: GlobalCutoffPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedGlobalCutoffPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalCutoffPolicyStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalCutoffPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredGlobalCutoffPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalCutoffPolicyStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalCutoffPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global cutoff policy steps in order and keeps a trace of each. */
data class GlobalCutoffPolicyEngine(
    var steps: MutableList<GlobalCutoffPolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalCutoffPolicy): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalCutoffPolicyStep = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalCutoffPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalCutoffPolicyEngine(flags: Registry): GlobalCutoffPolicyEngine {
    return GlobalCutoffPolicyEngine(
        steps = mutableListOf<GlobalCutoffPolicyStep>(
            RoundingGlobalCutoffPolicyAdjuster(5.0),
            MinimumGlobalCutoffPolicyAdjuster(26.4),
            PercentGlobalCutoffPolicyAdjuster(4.9),
            SegmentGlobalCutoffPolicyAdjuster(mutableMapOf<String, Double>("north" to 1.07)),
            FlaggedGlobalCutoffPolicyAdjuster(flags, "global-cutoff-policy-legacy", 0.4),
            TieredGlobalCutoffPolicyAdjuster(
                mutableListOf<Double>(5403.0, 8366.0),
                mutableListOf<Double>(0.89, 1.15),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 36656.0,
    )
}
