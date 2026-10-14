package com.acme.transport.emissions.routeoffset.offsetrule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted route offset template calculation. */
interface WeightedRouteOffsetTemplateModifier {
    fun describe(): String
    fun adjust(item: WeightedRouteOffsetTemplate, value: Double): Double
}

data class TieredWeightedRouteOffsetTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedRouteOffsetTemplateModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedRouteOffsetTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingWeightedRouteOffsetTemplateAdjuster(var step: Double) : WeightedRouteOffsetTemplateModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedRouteOffsetTemplate, value: Double): Double = roundTo(value, step)
}

data class FlaggedWeightedRouteOffsetTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedRouteOffsetTemplateModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedRouteOffsetTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapWeightedRouteOffsetTemplateAdjuster(var cap: Double) : WeightedRouteOffsetTemplateModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedRouteOffsetTemplate, value: Double): Double = minOf(value, cap)
}

data class TitleWeightedRouteOffsetTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedRouteOffsetTemplateModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: WeightedRouteOffsetTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the weighted route offset template steps in order and keeps a trace of each. */
data class WeightedRouteOffsetTemplateEngine(
    var steps: MutableList<WeightedRouteOffsetTemplateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedRouteOffsetTemplate): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedRouteOffsetTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedRouteOffsetTemplateEngine(flags: Registry): WeightedRouteOffsetTemplateEngine {
    return WeightedRouteOffsetTemplateEngine(
        steps = mutableListOf<WeightedRouteOffsetTemplateModifier>(
            TieredWeightedRouteOffsetTemplateAdjuster(
                mutableListOf<Double>(4470.0, 4918.0),
                mutableListOf<Double>(1.16, 0.84),
            ),
            RoundingWeightedRouteOffsetTemplateAdjuster(1.0),
            FlaggedWeightedRouteOffsetTemplateAdjuster(flags, "weighted-route-offset-template-strict", 9.7),
            CapWeightedRouteOffsetTemplateAdjuster(12982.0),
            TitleWeightedRouteOffsetTemplateAdjuster(
                mutableMapOf<String, Double>("retail" to 1.11, "north" to 0.99),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 2185.9,
    )
}
