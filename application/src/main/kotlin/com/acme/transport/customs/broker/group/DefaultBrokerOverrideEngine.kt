package com.acme.transport.customs.broker.group

import com.acme.transport.kit.flags.Registry

/** One step of the default broker override calculation. */
interface DefaultBrokerOverrideStep {
    fun describe(): String
    fun adjust(item: DefaultBrokerOverride, value: Double): Double
}

data class FlaggedDefaultBrokerOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultBrokerOverrideStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultBrokerOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ChannelDefaultBrokerOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultBrokerOverrideStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: DefaultBrokerOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapDefaultBrokerOverrideAdjuster(var cap: Double) : DefaultBrokerOverrideStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultBrokerOverride, value: Double): Double = minOf(value, cap)
}

data class DefaultBrokerOverrideRevisionOffsetAdjuster(var weight: Double) : DefaultBrokerOverrideStep {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: DefaultBrokerOverride, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

/** Runs the default broker override steps in order and keeps a trace of each. */
data class DefaultBrokerOverrideEngine(
    var steps: MutableList<DefaultBrokerOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultBrokerOverride): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultBrokerOverrideStep = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultBrokerOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultBrokerOverrideEngine(flags: Registry): DefaultBrokerOverrideEngine {
    return DefaultBrokerOverrideEngine(
        steps = mutableListOf<DefaultBrokerOverrideStep>(
            FlaggedDefaultBrokerOverrideAdjuster(flags, "default-broker-override-tiered", 3.8),
            ChannelDefaultBrokerOverrideAdjuster(
                mutableMapOf<String, Double>("online" to 1.21, "east" to 0.77, "north" to 1.0),
            ),
            CapDefaultBrokerOverrideAdjuster(49233.0),
            DefaultBrokerOverrideRevisionOffsetAdjuster(0.33),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 23875.2,
    )
}
