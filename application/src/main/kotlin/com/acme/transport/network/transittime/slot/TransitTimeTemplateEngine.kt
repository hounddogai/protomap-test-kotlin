package com.acme.transport.network.transittime.slot

import com.acme.transport.kit.numbers.roundTo

/** One step of the transit time template calculation. */
interface TransitTimeTemplateAdjuster {
    fun describe(): String
    fun adjust(item: TransitTimeTemplate, value: Double): Double
}

data class RoundingTransitTimeTemplateAdjuster(var step: Double) : TransitTimeTemplateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TransitTimeTemplate, value: Double): Double = roundTo(value, step)
}

data class TieredTransitTimeTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TransitTimeTemplateAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TransitTimeTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumTransitTimeTemplateAdjuster(var minimum: Double) : TransitTimeTemplateAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TransitTimeTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the transit time template steps in order and keeps a trace of each. */
data class TransitTimeTemplateEngine(
    var steps: MutableList<TransitTimeTemplateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TransitTimeTemplate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TransitTimeTemplateAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TransitTimeTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTransitTimeTemplateEngine(): TransitTimeTemplateEngine {
    return TransitTimeTemplateEngine(
        steps = mutableListOf<TransitTimeTemplateAdjuster>(
            RoundingTransitTimeTemplateAdjuster(0.25),
            TieredTransitTimeTemplateAdjuster(
                mutableListOf<Double>(1478.0, 2839.0, 4224.0),
                mutableListOf<Double>(0.97, 0.91, 1.1),
            ),
            MinimumTransitTimeTemplateAdjuster(44.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 74591.8,
    )
}
