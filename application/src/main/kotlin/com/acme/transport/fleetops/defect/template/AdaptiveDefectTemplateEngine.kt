package com.acme.transport.fleetops.defect.template

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive defect template calculation. */
interface AdaptiveDefectTemplateFactor {
    fun describe(): String
    fun adjust(item: AdaptiveDefectTemplate, value: Double): Double
}

data class TieredAdaptiveDefectTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveDefectTemplateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveDefectTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class AdaptiveDefectTemplateQuantityOffsetAdjuster(var weight: Double) : AdaptiveDefectTemplateFactor {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: AdaptiveDefectTemplate, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class RoundingAdaptiveDefectTemplateAdjuster(var step: Double) : AdaptiveDefectTemplateFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveDefectTemplate, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive defect template steps in order and keeps a trace of each. */
data class AdaptiveDefectTemplateEngine(
    var steps: MutableList<AdaptiveDefectTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveDefectTemplate): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveDefectTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveDefectTemplateEngine(): AdaptiveDefectTemplateEngine {
    return AdaptiveDefectTemplateEngine(
        steps = mutableListOf<AdaptiveDefectTemplateFactor>(
            TieredAdaptiveDefectTemplateAdjuster(
                mutableListOf<Double>(8021.0, 8432.0),
                mutableListOf<Double>(1.01, 1.2),
            ),
            AdaptiveDefectTemplateQuantityOffsetAdjuster(0.41),
            RoundingAdaptiveDefectTemplateAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 76096.2,
    )
}
