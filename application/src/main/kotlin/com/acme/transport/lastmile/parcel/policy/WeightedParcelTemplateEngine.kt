package com.acme.transport.lastmile.parcel.policy

import com.acme.transport.kit.flags.Registry

/** One step of the weighted parcel template calculation. */
interface WeightedParcelTemplateAdjuster {
    fun describe(): String
    fun adjust(item: WeightedParcelTemplate, value: Double): Double
}

data class CapWeightedParcelTemplateAdjuster(var cap: Double) : WeightedParcelTemplateAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedParcelTemplate, value: Double): Double = minOf(value, cap)
}

data class MinimumWeightedParcelTemplateAdjuster(var minimum: Double) : WeightedParcelTemplateAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedParcelTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedWeightedParcelTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedParcelTemplateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedParcelTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted parcel template steps in order and keeps a trace of each. */
data class WeightedParcelTemplateEngine(
    var steps: MutableList<WeightedParcelTemplateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedParcelTemplate): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedParcelTemplateAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedParcelTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedParcelTemplateEngine(flags: Registry): WeightedParcelTemplateEngine {
    return WeightedParcelTemplateEngine(
        steps = mutableListOf<WeightedParcelTemplateAdjuster>(
            CapWeightedParcelTemplateAdjuster(13141.0),
            MinimumWeightedParcelTemplateAdjuster(9.6),
            FlaggedWeightedParcelTemplateAdjuster(flags, "weighted-parcel-template-manual", 3.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 32565.1,
    )
}
