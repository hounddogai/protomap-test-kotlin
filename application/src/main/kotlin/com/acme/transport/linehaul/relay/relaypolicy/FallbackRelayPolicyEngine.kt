package com.acme.transport.linehaul.relay.relaypolicy

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback relay policy calculation. */
interface FallbackRelayPolicyStep {
    fun describe(): String
    fun adjust(item: FallbackRelayPolicy, value: Double): Double
}

data class TieredFallbackRelayPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackRelayPolicyStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackRelayPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentFallbackRelayPolicyAdjuster(var percent: Double) : FallbackRelayPolicyStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackRelayPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingFallbackRelayPolicyAdjuster(var step: Double) : FallbackRelayPolicyStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackRelayPolicy, value: Double): Double = roundTo(value, step)
}

data class TitleFallbackRelayPolicyAdjuster(var factors: MutableMap<String, Double>) : FallbackRelayPolicyStep {
    override fun describe(): String = "by title"

    override fun adjust(item: FallbackRelayPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumFallbackRelayPolicyAdjuster(var minimum: Double) : FallbackRelayPolicyStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackRelayPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fallback relay policy steps in order and keeps a trace of each. */
data class FallbackRelayPolicyEngine(
    var steps: MutableList<FallbackRelayPolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackRelayPolicy): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackRelayPolicyStep = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackRelayPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackRelayPolicyEngine(): FallbackRelayPolicyEngine {
    return FallbackRelayPolicyEngine(
        steps = mutableListOf<FallbackRelayPolicyStep>(
            TieredFallbackRelayPolicyAdjuster(
                mutableListOf<Double>(2123.0, 2999.0, 4753.0, 8236.0),
                mutableListOf<Double>(1.08, 1.1, 1.09, 1.14),
            ),
            PercentFallbackRelayPolicyAdjuster(22.1),
            RoundingFallbackRelayPolicyAdjuster(5.0),
            TitleFallbackRelayPolicyAdjuster(mutableMapOf<String, Double>("online" to 1.33, "central" to 1.02)),
            MinimumFallbackRelayPolicyAdjuster(13.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 54374.7,
    )
}
