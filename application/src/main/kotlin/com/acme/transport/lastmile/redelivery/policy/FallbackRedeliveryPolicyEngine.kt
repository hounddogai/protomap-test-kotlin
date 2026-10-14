package com.acme.transport.lastmile.redelivery.policy

import com.acme.transport.kit.flags.Registry

/** One step of the fallback redelivery policy calculation. */
interface FallbackRedeliveryPolicyFactor {
    fun describe(): String
    fun adjust(item: FallbackRedeliveryPolicy, value: Double): Double
}

data class CapFallbackRedeliveryPolicyAdjuster(var cap: Double) : FallbackRedeliveryPolicyFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackRedeliveryPolicy, value: Double): Double = minOf(value, cap)
}

data class TieredFallbackRedeliveryPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackRedeliveryPolicyFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackRedeliveryPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentFallbackRedeliveryPolicyAdjuster(var percent: Double) : FallbackRedeliveryPolicyFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackRedeliveryPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedFallbackRedeliveryPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackRedeliveryPolicyFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackRedeliveryPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumFallbackRedeliveryPolicyAdjuster(var minimum: Double) : FallbackRedeliveryPolicyFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackRedeliveryPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ChannelFallbackRedeliveryPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackRedeliveryPolicyFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: FallbackRedeliveryPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fallback redelivery policy steps in order and keeps a trace of each. */
data class FallbackRedeliveryPolicyEngine(
    var steps: MutableList<FallbackRedeliveryPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackRedeliveryPolicy): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackRedeliveryPolicyFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackRedeliveryPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackRedeliveryPolicyEngine(flags: Registry): FallbackRedeliveryPolicyEngine {
    return FallbackRedeliveryPolicyEngine(
        steps = mutableListOf<FallbackRedeliveryPolicyFactor>(
            CapFallbackRedeliveryPolicyAdjuster(6417.0),
            TieredFallbackRedeliveryPolicyAdjuster(
                mutableListOf<Double>(3749.0, 6536.0, 6592.0, 7182.0),
                mutableListOf<Double>(1.03, 1.03, 0.98, 1.13),
            ),
            PercentFallbackRedeliveryPolicyAdjuster(-6.9),
            FlaggedFallbackRedeliveryPolicyAdjuster(flags, "fallback-redelivery-policy-lenient", -3.1),
            MinimumFallbackRedeliveryPolicyAdjuster(15.0),
            ChannelFallbackRedeliveryPolicyAdjuster(
                mutableMapOf<String, Double>("east" to 1.05, "retail" to 0.94),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 36878.7,
    )
}
