package com.acme.transport.crewing.restbreak.breakmapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted rest break template calculation. */
interface WeightedRestBreakTemplateFactor {
    fun describe(): String
    fun adjust(item: WeightedRestBreakTemplate, value: Double): Double
}

data class RoundingWeightedRestBreakTemplateAdjuster(var step: Double) : WeightedRestBreakTemplateFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedRestBreakTemplate, value: Double): Double = roundTo(value, step)
}

data class ChannelWeightedRestBreakTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedRestBreakTemplateFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: WeightedRestBreakTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredWeightedRestBreakTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedRestBreakTemplateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedRestBreakTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedWeightedRestBreakTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedRestBreakTemplateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedRestBreakTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapWeightedRestBreakTemplateAdjuster(var cap: Double) : WeightedRestBreakTemplateFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedRestBreakTemplate, value: Double): Double = minOf(value, cap)
}

/** Runs the weighted rest break template steps in order and keeps a trace of each. */
data class WeightedRestBreakTemplateEngine(
    var steps: MutableList<WeightedRestBreakTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedRestBreakTemplate): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedRestBreakTemplateFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedRestBreakTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedRestBreakTemplateEngine(flags: Registry): WeightedRestBreakTemplateEngine {
    return WeightedRestBreakTemplateEngine(
        steps = mutableListOf<WeightedRestBreakTemplateFactor>(
            RoundingWeightedRestBreakTemplateAdjuster(0.5),
            ChannelWeightedRestBreakTemplateAdjuster(
                mutableMapOf<String, Double>("west" to 1.17, "central" to 0.77),
            ),
            TieredWeightedRestBreakTemplateAdjuster(
                mutableListOf<Double>(2589.0, 3773.0, 7859.0, 7923.0),
                mutableListOf<Double>(0.97, 1.13, 1.1, 1.0),
            ),
            FlaggedWeightedRestBreakTemplateAdjuster(flags, "weighted-rest-break-template-capped", -2.1),
            CapWeightedRestBreakTemplateAdjuster(30595.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 86839.0,
    )
}
