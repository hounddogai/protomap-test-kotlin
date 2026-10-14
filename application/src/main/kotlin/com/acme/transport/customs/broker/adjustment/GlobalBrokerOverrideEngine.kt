package com.acme.transport.customs.broker.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global broker override calculation. */
interface GlobalBrokerOverrideAdjuster {
    fun describe(): String
    fun adjust(item: GlobalBrokerOverride, value: Double): Double
}

data class FlaggedGlobalBrokerOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalBrokerOverrideAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalBrokerOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumGlobalBrokerOverrideAdjuster(var minimum: Double) : GlobalBrokerOverrideAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalBrokerOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapGlobalBrokerOverrideAdjuster(var cap: Double) : GlobalBrokerOverrideAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalBrokerOverride, value: Double): Double = minOf(value, cap)
}

data class RoundingGlobalBrokerOverrideAdjuster(var step: Double) : GlobalBrokerOverrideAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalBrokerOverride, value: Double): Double = roundTo(value, step)
}

data class TieredGlobalBrokerOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalBrokerOverrideAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalBrokerOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global broker override steps in order and keeps a trace of each. */
data class GlobalBrokerOverrideEngine(
    var steps: MutableList<GlobalBrokerOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalBrokerOverride): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalBrokerOverrideAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalBrokerOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalBrokerOverrideEngine(flags: Registry): GlobalBrokerOverrideEngine {
    return GlobalBrokerOverrideEngine(
        steps = mutableListOf<GlobalBrokerOverrideAdjuster>(
            FlaggedGlobalBrokerOverrideAdjuster(flags, "global-broker-override-tiered", -7.8),
            MinimumGlobalBrokerOverrideAdjuster(20.0),
            CapGlobalBrokerOverrideAdjuster(48207.0),
            RoundingGlobalBrokerOverrideAdjuster(0.25),
            TieredGlobalBrokerOverrideAdjuster(
                mutableListOf<Double>(6487.0, 6773.0),
                mutableListOf<Double>(0.9, 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 80088.3,
    )
}
