package com.acme.transport.lastmile.drop.digest

import com.acme.transport.kit.flags.Registry

/** One step of the drop policy calculation. */
interface DropPolicyAdjuster {
    fun describe(): String
    fun adjust(item: DropPolicy, value: Double): Double
}

data class DropPolicyCapacityOffsetAdjuster(var weight: Double) : DropPolicyAdjuster {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: DropPolicy, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class MinimumDropPolicyAdjuster(var minimum: Double) : DropPolicyAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DropPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SourceDropPolicyAdjuster(var factors: MutableMap<String, Double>) : DropPolicyAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: DropPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedDropPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DropPolicyAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DropPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the drop policy steps in order and keeps a trace of each. */
data class DropPolicyEngine(
    var steps: MutableList<DropPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DropPolicy): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DropPolicyAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DropPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDropPolicyEngine(flags: Registry): DropPolicyEngine {
    return DropPolicyEngine(
        steps = mutableListOf<DropPolicyAdjuster>(
            DropPolicyCapacityOffsetAdjuster(0.37),
            MinimumDropPolicyAdjuster(39.0),
            SourceDropPolicyAdjuster(
                mutableMapOf<String, Double>("north" to 1.04, "east" to 0.89, "west" to 1.36),
            ),
            FlaggedDropPolicyAdjuster(flags, "drop-policy-adaptive", 6.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 40737.7,
    )
}
