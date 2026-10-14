package com.acme.transport.crewing.swap.swapnotice

import com.acme.transport.kit.flags.Registry

/** One step of the swap template calculation. */
interface SwapTemplateFactor {
    fun describe(): String
    fun adjust(item: SwapTemplate, value: Double): Double
}

data class TieredSwapTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SwapTemplateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SwapTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedSwapTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SwapTemplateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SwapTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumSwapTemplateAdjuster(var minimum: Double) : SwapTemplateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SwapTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the swap template steps in order and keeps a trace of each. */
data class SwapTemplateEngine(
    var steps: MutableList<SwapTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SwapTemplate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SwapTemplateFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SwapTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSwapTemplateEngine(flags: Registry): SwapTemplateEngine {
    return SwapTemplateEngine(
        steps = mutableListOf<SwapTemplateFactor>(
            TieredSwapTemplateAdjuster(
                mutableListOf<Double>(3135.0, 5931.0, 9807.0),
                mutableListOf<Double>(0.96, 0.83, 0.81),
            ),
            FlaggedSwapTemplateAdjuster(flags, "swap-template-fallback", 3.1),
            MinimumSwapTemplateAdjuster(9.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 44928.9,
    )
}
