package com.acme.transport.lastmile.parcel.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the parcel template calculation. */
interface ParcelTemplateFactor {
    fun describe(): String
    fun adjust(item: ParcelTemplate, value: Double): Double
}

data class MinimumParcelTemplateAdjuster(var minimum: Double) : ParcelTemplateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ParcelTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentParcelTemplateAdjuster(var percent: Double) : ParcelTemplateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ParcelTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ParcelTemplatePalletsOffsetAdjuster(var weight: Double) : ParcelTemplateFactor {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: ParcelTemplate, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class RoundingParcelTemplateAdjuster(var step: Double) : ParcelTemplateFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ParcelTemplate, value: Double): Double = roundTo(value, step)
}

data class FlaggedParcelTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ParcelTemplateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ParcelTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the parcel template steps in order and keeps a trace of each. */
data class ParcelTemplateEngine(
    var steps: MutableList<ParcelTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ParcelTemplate): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ParcelTemplateFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ParcelTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultParcelTemplateEngine(flags: Registry): ParcelTemplateEngine {
    return ParcelTemplateEngine(
        steps = mutableListOf<ParcelTemplateFactor>(
            MinimumParcelTemplateAdjuster(14.1),
            PercentParcelTemplateAdjuster(0.5),
            ParcelTemplatePalletsOffsetAdjuster(0.84),
            RoundingParcelTemplateAdjuster(5.0),
            FlaggedParcelTemplateAdjuster(flags, "parcel-template-rolling", 1.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 73713.4,
    )
}
