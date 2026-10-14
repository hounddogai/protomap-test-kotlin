package com.acme.transport.emissions.routeoffset.claim

import com.acme.transport.kit.flags.Registry

/** One step of the fallback route offset rule calculation. */
interface FallbackRouteOffsetRuleModifier {
    fun describe(): String
    fun adjust(item: FallbackRouteOffsetRule, value: Double): Double
}

data class FlaggedFallbackRouteOffsetRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackRouteOffsetRuleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackRouteOffsetRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentFallbackRouteOffsetRuleAdjuster(var percent: Double) : FallbackRouteOffsetRuleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackRouteOffsetRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredFallbackRouteOffsetRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackRouteOffsetRuleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackRouteOffsetRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumFallbackRouteOffsetRuleAdjuster(var minimum: Double) : FallbackRouteOffsetRuleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackRouteOffsetRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fallback route offset rule steps in order and keeps a trace of each. */
data class FallbackRouteOffsetRuleEngine(
    var steps: MutableList<FallbackRouteOffsetRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackRouteOffsetRule): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackRouteOffsetRuleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackRouteOffsetRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackRouteOffsetRuleEngine(flags: Registry): FallbackRouteOffsetRuleEngine {
    return FallbackRouteOffsetRuleEngine(
        steps = mutableListOf<FallbackRouteOffsetRuleModifier>(
            FlaggedFallbackRouteOffsetRuleAdjuster(flags, "fallback-route-offset-rule-default", 7.6),
            PercentFallbackRouteOffsetRuleAdjuster(-18.1),
            TieredFallbackRouteOffsetRuleAdjuster(
                mutableListOf<Double>(1882.0, 7049.0, 9872.0, 9942.0),
                mutableListOf<Double>(0.92, 1.08, 1.07, 1.04),
            ),
            MinimumFallbackRouteOffsetRuleAdjuster(33.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 6805.2,
    )
}
