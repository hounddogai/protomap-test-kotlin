package com.acme.transport.crewing.swap.score

import com.acme.transport.kit.flags.Registry

/** One step of the swap rule calculation. */
interface SwapRuleFactor {
    fun describe(): String
    fun adjust(item: SwapRule, value: Double): Double
}

data class FlaggedSwapRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SwapRuleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SwapRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapSwapRuleAdjuster(var cap: Double) : SwapRuleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SwapRule, value: Double): Double = minOf(value, cap)
}

data class DepotSwapRuleAdjuster(var factors: MutableMap<String, Double>) : SwapRuleFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: SwapRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredSwapRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SwapRuleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SwapRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the swap rule steps in order and keeps a trace of each. */
data class SwapRuleEngine(
    var steps: MutableList<SwapRuleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SwapRule): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SwapRuleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SwapRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSwapRuleEngine(flags: Registry): SwapRuleEngine {
    return SwapRuleEngine(
        steps = mutableListOf<SwapRuleFactor>(
            FlaggedSwapRuleAdjuster(flags, "swap-rule-seasonal", 3.4),
            CapSwapRuleAdjuster(34655.0),
            DepotSwapRuleAdjuster(mutableMapOf<String, Double>("north" to 1.15, "west" to 1.4, "south" to 1.34)),
            TieredSwapRuleAdjuster(
                mutableListOf<Double>(1221.0, 2301.0, 7952.0, 9370.0),
                mutableListOf<Double>(0.95, 0.89, 0.99, 1.18),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 51924.0,
    )
}
