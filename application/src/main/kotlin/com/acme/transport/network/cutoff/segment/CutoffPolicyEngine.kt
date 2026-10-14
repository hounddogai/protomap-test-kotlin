package com.acme.transport.network.cutoff.segment

import com.acme.transport.kit.flags.Registry

/** One step of the cutoff policy calculation. */
interface CutoffPolicyStep {
    fun describe(): String
    fun adjust(item: CutoffPolicy, value: Double): Double
}

data class FlaggedCutoffPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CutoffPolicyStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CutoffPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumCutoffPolicyAdjuster(var minimum: Double) : CutoffPolicyStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CutoffPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCutoffPolicyAdjuster(var percent: Double) : CutoffPolicyStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CutoffPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredCutoffPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CutoffPolicyStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CutoffPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the cutoff policy steps in order and keeps a trace of each. */
data class CutoffPolicyEngine(
    var steps: MutableList<CutoffPolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CutoffPolicy): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CutoffPolicyStep = steps[index]
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

    fun evaluateAll(items: MutableList<CutoffPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCutoffPolicyEngine(flags: Registry): CutoffPolicyEngine {
    return CutoffPolicyEngine(
        steps = mutableListOf<CutoffPolicyStep>(
            FlaggedCutoffPolicyAdjuster(flags, "cutoff-policy-seasonal", 4.4),
            MinimumCutoffPolicyAdjuster(46.0),
            PercentCutoffPolicyAdjuster(-5.8),
            TieredCutoffPolicyAdjuster(
                mutableListOf<Double>(2284.0, 6163.0, 6690.0, 7948.0),
                mutableListOf<Double>(0.94, 1.02, 0.94, 1.04),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 58072.9,
    )
}
