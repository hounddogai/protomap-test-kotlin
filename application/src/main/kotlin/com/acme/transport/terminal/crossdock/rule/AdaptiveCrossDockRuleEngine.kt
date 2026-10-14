package com.acme.transport.terminal.crossdock.rule

import com.acme.transport.kit.flags.Registry

/** One step of the adaptive cross dock rule calculation. */
interface AdaptiveCrossDockRuleAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveCrossDockRule, value: Double): Double
}

data class MinimumAdaptiveCrossDockRuleAdjuster(var minimum: Double) : AdaptiveCrossDockRuleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveCrossDockRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameAdaptiveCrossDockRuleAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveCrossDockRuleAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: AdaptiveCrossDockRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapAdaptiveCrossDockRuleAdjuster(var cap: Double) : AdaptiveCrossDockRuleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveCrossDockRule, value: Double): Double = minOf(value, cap)
}

data class AdaptiveCrossDockRuleAmountOffsetAdjuster(var weight: Double) : AdaptiveCrossDockRuleAdjuster {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: AdaptiveCrossDockRule, value: Double): Double = value + item.amount * weight
}

data class FlaggedAdaptiveCrossDockRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveCrossDockRuleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveCrossDockRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the adaptive cross dock rule steps in order and keeps a trace of each. */
data class AdaptiveCrossDockRuleEngine(
    var steps: MutableList<AdaptiveCrossDockRuleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveCrossDockRule): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveCrossDockRuleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveCrossDockRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveCrossDockRuleEngine(flags: Registry): AdaptiveCrossDockRuleEngine {
    return AdaptiveCrossDockRuleEngine(
        steps = mutableListOf<AdaptiveCrossDockRuleAdjuster>(
            MinimumAdaptiveCrossDockRuleAdjuster(24.3),
            NameAdaptiveCrossDockRuleAdjuster(mutableMapOf<String, Double>("retail" to 1.14, "central" to 1.06)),
            CapAdaptiveCrossDockRuleAdjuster(28105.0),
            AdaptiveCrossDockRuleAmountOffsetAdjuster(1.52),
            FlaggedAdaptiveCrossDockRuleAdjuster(flags, "adaptive-cross-dock-rule-global", -7.7),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 86667.0,
    )
}
