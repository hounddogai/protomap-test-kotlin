package com.acme.transport.network.node.nodeagreement

import com.acme.transport.kit.flags.Registry

/** One step of the global node policy calculation. */
interface GlobalNodePolicyStep {
    fun describe(): String
    fun adjust(item: GlobalNodePolicy, value: Double): Double
}

data class ChannelGlobalNodePolicyAdjuster(var factors: MutableMap<String, Double>) : GlobalNodePolicyStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: GlobalNodePolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedGlobalNodePolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalNodePolicyStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalNodePolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class GlobalNodePolicyEndsAtOffsetAdjuster(var weight: Double) : GlobalNodePolicyStep {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: GlobalNodePolicy, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class TieredGlobalNodePolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalNodePolicyStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalNodePolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapGlobalNodePolicyAdjuster(var cap: Double) : GlobalNodePolicyStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalNodePolicy, value: Double): Double = minOf(value, cap)
}

/** Runs the global node policy steps in order and keeps a trace of each. */
data class GlobalNodePolicyEngine(
    var steps: MutableList<GlobalNodePolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalNodePolicy): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalNodePolicyStep = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalNodePolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalNodePolicyEngine(flags: Registry): GlobalNodePolicyEngine {
    return GlobalNodePolicyEngine(
        steps = mutableListOf<GlobalNodePolicyStep>(
            ChannelGlobalNodePolicyAdjuster(mutableMapOf<String, Double>("east" to 1.35, "west" to 0.98)),
            FlaggedGlobalNodePolicyAdjuster(flags, "global-node-policy-fallback", -4.0),
            GlobalNodePolicyEndsAtOffsetAdjuster(0.57),
            TieredGlobalNodePolicyAdjuster(
                mutableListOf<Double>(3833.0, 6968.0),
                mutableListOf<Double>(1.14, 1.2),
            ),
            CapGlobalNodePolicyAdjuster(19177.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 87505.9,
    )
}
