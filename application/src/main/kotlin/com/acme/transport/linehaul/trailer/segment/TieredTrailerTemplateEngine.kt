package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered trailer template calculation. */
interface TieredTrailerTemplateFactor {
    fun describe(): String
    fun adjust(item: TieredTrailerTemplate, value: Double): Double
}

data class FlaggedTieredTrailerTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredTrailerTemplateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredTrailerTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingTieredTrailerTemplateAdjuster(var step: Double) : TieredTrailerTemplateFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredTrailerTemplate, value: Double): Double = roundTo(value, step)
}

data class MinimumTieredTrailerTemplateAdjuster(var minimum: Double) : TieredTrailerTemplateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredTrailerTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the tiered trailer template steps in order and keeps a trace of each. */
data class TieredTrailerTemplateEngine(
    var steps: MutableList<TieredTrailerTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredTrailerTemplate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredTrailerTemplateFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TieredTrailerTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredTrailerTemplateEngine(flags: Registry): TieredTrailerTemplateEngine {
    return TieredTrailerTemplateEngine(
        steps = mutableListOf<TieredTrailerTemplateFactor>(
            FlaggedTieredTrailerTemplateAdjuster(flags, "tiered-trailer-template-regional", -6.2),
            RoundingTieredTrailerTemplateAdjuster(0.25),
            MinimumTieredTrailerTemplateAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 7625.0,
    )
}
