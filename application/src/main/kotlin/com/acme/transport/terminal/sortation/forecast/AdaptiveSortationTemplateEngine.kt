package com.acme.transport.terminal.sortation.forecast

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive sortation template calculation. */
interface AdaptiveSortationTemplateModifier {
    fun describe(): String
    fun adjust(item: AdaptiveSortationTemplate, value: Double): Double
}

data class TieredAdaptiveSortationTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveSortationTemplateModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveSortationTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumAdaptiveSortationTemplateAdjuster(var minimum: Double) : AdaptiveSortationTemplateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveSortationTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class AdaptiveSortationTemplateDiscountOffsetAdjuster(
    var weight: Double,
) : AdaptiveSortationTemplateModifier {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: AdaptiveSortationTemplate, value: Double): Double = value + item.discount * weight
}

/** Runs the adaptive sortation template steps in order and keeps a trace of each. */
data class AdaptiveSortationTemplateEngine(
    var steps: MutableList<AdaptiveSortationTemplateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveSortationTemplate): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveSortationTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveSortationTemplateEngine(): AdaptiveSortationTemplateEngine {
    return AdaptiveSortationTemplateEngine(
        steps = mutableListOf<AdaptiveSortationTemplateModifier>(
            TieredAdaptiveSortationTemplateAdjuster(
                mutableListOf<Double>(4224.0, 5836.0),
                mutableListOf<Double>(0.91, 0.85),
            ),
            MinimumAdaptiveSortationTemplateAdjuster(17.9),
            AdaptiveSortationTemplateDiscountOffsetAdjuster(1.56),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 8473.6,
    )
}
