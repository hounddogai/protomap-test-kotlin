package com.acme.transport.terminal.crossdock.template

import com.acme.transport.kit.numbers.roundTo

/** One step of the cross dock template calculation. */
interface CrossDockTemplateStep {
    fun describe(): String
    fun adjust(item: CrossDockTemplate, value: Double): Double
}

data class MinimumCrossDockTemplateAdjuster(var minimum: Double) : CrossDockTemplateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CrossDockTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCrossDockTemplateAdjuster(var step: Double) : CrossDockTemplateStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CrossDockTemplate, value: Double): Double = roundTo(value, step)
}

data class UnitCrossDockTemplateAdjuster(var factors: MutableMap<String, Double>) : CrossDockTemplateStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: CrossDockTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCrossDockTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CrossDockTemplateStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CrossDockTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the cross dock template steps in order and keeps a trace of each. */
data class CrossDockTemplateEngine(
    var steps: MutableList<CrossDockTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CrossDockTemplate): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CrossDockTemplateStep = steps[index]
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

    fun evaluateAll(items: MutableList<CrossDockTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCrossDockTemplateEngine(): CrossDockTemplateEngine {
    return CrossDockTemplateEngine(
        steps = mutableListOf<CrossDockTemplateStep>(
            MinimumCrossDockTemplateAdjuster(37.7),
            RoundingCrossDockTemplateAdjuster(5.0),
            UnitCrossDockTemplateAdjuster(mutableMapOf<String, Double>("south" to 1.0, "retail" to 0.93)),
            TieredCrossDockTemplateAdjuster(
                mutableListOf<Double>(1648.0, 7139.0),
                mutableListOf<Double>(0.98, 0.83),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 97549.6,
    )
}
