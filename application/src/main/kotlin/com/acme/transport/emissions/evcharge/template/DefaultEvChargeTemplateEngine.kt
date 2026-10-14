package com.acme.transport.emissions.evcharge.template

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default ev charge template calculation. */
interface DefaultEvChargeTemplateStep {
    fun describe(): String
    fun adjust(item: DefaultEvChargeTemplate, value: Double): Double
}

data class DefaultEvChargeTemplateScoreOffsetAdjuster(var weight: Double) : DefaultEvChargeTemplateStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: DefaultEvChargeTemplate, value: Double): Double = value + item.score * weight
}

data class CapDefaultEvChargeTemplateAdjuster(var cap: Double) : DefaultEvChargeTemplateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultEvChargeTemplate, value: Double): Double = minOf(value, cap)
}

data class PercentDefaultEvChargeTemplateAdjuster(var percent: Double) : DefaultEvChargeTemplateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultEvChargeTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionDefaultEvChargeTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultEvChargeTemplateStep {
    override fun describe(): String = "by region"

    override fun adjust(item: DefaultEvChargeTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumDefaultEvChargeTemplateAdjuster(var minimum: Double) : DefaultEvChargeTemplateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultEvChargeTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the default ev charge template steps in order and keeps a trace of each. */
data class DefaultEvChargeTemplateEngine(
    var steps: MutableList<DefaultEvChargeTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultEvChargeTemplate): Double {
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

    fun evaluateAll(items: MutableList<DefaultEvChargeTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultEvChargeTemplateEngine(): DefaultEvChargeTemplateEngine {
    return DefaultEvChargeTemplateEngine(
        steps = mutableListOf<DefaultEvChargeTemplateStep>(
            DefaultEvChargeTemplateScoreOffsetAdjuster(1.0),
            CapDefaultEvChargeTemplateAdjuster(33331.0),
            PercentDefaultEvChargeTemplateAdjuster(17.2),
            RegionDefaultEvChargeTemplateAdjuster(mutableMapOf<String, Double>("west" to 0.92)),
            MinimumDefaultEvChargeTemplateAdjuster(17.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 85319.5,
    )
}
