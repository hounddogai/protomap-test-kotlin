package com.acme.transport.emissions.emissionfactor.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted emission factor template calculation. */
interface WeightedEmissionFactorTemplateAdjuster {
    fun describe(): String
    fun adjust(item: WeightedEmissionFactorTemplate, value: Double): Double
}

data class FlaggedWeightedEmissionFactorTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedEmissionFactorTemplateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedEmissionFactorTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class WeightedEmissionFactorTemplatePriorityOffsetAdjuster(
    var weight: Double,
) : WeightedEmissionFactorTemplateAdjuster {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: WeightedEmissionFactorTemplate, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

data class RoundingWeightedEmissionFactorTemplateAdjuster(
    var step: Double,
) : WeightedEmissionFactorTemplateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedEmissionFactorTemplate, value: Double): Double = roundTo(value, step)
}

data class CapWeightedEmissionFactorTemplateAdjuster(var cap: Double) : WeightedEmissionFactorTemplateAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedEmissionFactorTemplate, value: Double): Double = minOf(value, cap)
}

data class CategoryWeightedEmissionFactorTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedEmissionFactorTemplateAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: WeightedEmissionFactorTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumWeightedEmissionFactorTemplateAdjuster(
    var minimum: Double,
) : WeightedEmissionFactorTemplateAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedEmissionFactorTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the weighted emission factor template steps in order and keeps a trace of each. */
data class WeightedEmissionFactorTemplateEngine(
    var steps: MutableList<WeightedEmissionFactorTemplateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedEmissionFactorTemplate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedEmissionFactorTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedEmissionFactorTemplateEngine(flags: Registry): WeightedEmissionFactorTemplateEngine {
    return WeightedEmissionFactorTemplateEngine(
        steps = mutableListOf<WeightedEmissionFactorTemplateAdjuster>(
            FlaggedWeightedEmissionFactorTemplateAdjuster(
                flags,
                "weighted-emission-factor-template-legacy",
                -5.0,
            ),
            WeightedEmissionFactorTemplatePriorityOffsetAdjuster(0.48),
            RoundingWeightedEmissionFactorTemplateAdjuster(1.0),
            CapWeightedEmissionFactorTemplateAdjuster(7521.0),
            CategoryWeightedEmissionFactorTemplateAdjuster(
                mutableMapOf<String, Double>("south" to 1.07, "east" to 0.91),
            ),
            MinimumWeightedEmissionFactorTemplateAdjuster(37.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 75263.0,
    )
}
