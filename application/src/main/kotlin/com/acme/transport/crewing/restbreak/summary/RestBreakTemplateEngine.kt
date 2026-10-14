package com.acme.transport.crewing.restbreak.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rest break template calculation. */
interface RestBreakTemplateFactor {
    fun describe(): String
    fun adjust(item: RestBreakTemplate, value: Double): Double
}

data class TieredRestBreakTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RestBreakTemplateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RestBreakTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRestBreakTemplateAdjuster(var minimum: Double) : RestBreakTemplateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RestBreakTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRestBreakTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RestBreakTemplateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RestBreakTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rest break template steps in order and keeps a trace of each. */
data class RestBreakTemplateEngine(
    var steps: MutableList<RestBreakTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RestBreakTemplate): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RestBreakTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRestBreakTemplateEngine(flags: Registry): RestBreakTemplateEngine {
    return RestBreakTemplateEngine(
        steps = mutableListOf<RestBreakTemplateFactor>(
            TieredRestBreakTemplateAdjuster(
                mutableListOf<Double>(8628.0, 8636.0, 8955.0),
                mutableListOf<Double>(1.06, 1.03, 1.1),
            ),
            MinimumRestBreakTemplateAdjuster(42.6),
            FlaggedRestBreakTemplateAdjuster(flags, "rest-break-template-adaptive", 0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 47088.7,
    )
}
