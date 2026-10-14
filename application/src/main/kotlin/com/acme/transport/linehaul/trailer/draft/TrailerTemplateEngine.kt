package com.acme.transport.linehaul.trailer.draft

import com.acme.transport.kit.flags.Registry

/** One step of the trailer template calculation. */
interface TrailerTemplateFactor {
    fun describe(): String
    fun adjust(item: TrailerTemplate, value: Double): Double
}

data class FlaggedTrailerTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerTemplateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ChannelTrailerTemplateAdjuster(var factors: MutableMap<String, Double>) : TrailerTemplateFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: TrailerTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredTrailerTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerTemplateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the trailer template steps in order and keeps a trace of each. */
data class TrailerTemplateEngine(
    var steps: MutableList<TrailerTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TrailerTemplate): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TrailerTemplateFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TrailerTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTrailerTemplateEngine(flags: Registry): TrailerTemplateEngine {
    return TrailerTemplateEngine(
        steps = mutableListOf<TrailerTemplateFactor>(
            FlaggedTrailerTemplateAdjuster(flags, "trailer-template-pending", -5.7),
            ChannelTrailerTemplateAdjuster(mutableMapOf<String, Double>("retail" to 0.99)),
            TieredTrailerTemplateAdjuster(
                mutableListOf<Double>(5034.0, 8733.0),
                mutableListOf<Double>(0.97, 0.92),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 30992.3,
    )
}
