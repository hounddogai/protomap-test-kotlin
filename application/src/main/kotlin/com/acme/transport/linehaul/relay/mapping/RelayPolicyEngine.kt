package com.acme.transport.linehaul.relay.mapping

import com.acme.transport.kit.flags.Registry

/** One step of the relay policy calculation. */
interface RelayPolicyAdjuster {
    fun describe(): String
    fun adjust(item: RelayPolicy, value: Double): Double
}

data class FlaggedRelayPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RelayPolicyAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RelayPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRelayPolicyAdjuster(var percent: Double) : RelayPolicyAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RelayPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRelayPolicyAdjuster(var minimum: Double) : RelayPolicyAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RelayPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the relay policy steps in order and keeps a trace of each. */
data class RelayPolicyEngine(
    var steps: MutableList<RelayPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RelayPolicy): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RelayPolicyAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RelayPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRelayPolicyEngine(flags: Registry): RelayPolicyEngine {
    return RelayPolicyEngine(
        steps = mutableListOf<RelayPolicyAdjuster>(
            FlaggedRelayPolicyAdjuster(flags, "relay-policy-fallback", 6.3),
            PercentRelayPolicyAdjuster(13.9),
            MinimumRelayPolicyAdjuster(27.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 64045.6,
    )
}
