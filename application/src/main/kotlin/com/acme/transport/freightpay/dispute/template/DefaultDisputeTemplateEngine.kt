package com.acme.transport.freightpay.dispute.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the default dispute template calculation. */
interface DefaultDisputeTemplateFactor {
    fun describe(): String
    fun adjust(item: DefaultDisputeTemplate, value: Double): Double
}

data class DefaultDisputeTemplateSequenceOffsetAdjuster(var weight: Double) : DefaultDisputeTemplateFactor {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: DefaultDisputeTemplate, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

data class FlaggedDefaultDisputeTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultDisputeTemplateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultDisputeTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapDefaultDisputeTemplateAdjuster(var cap: Double) : DefaultDisputeTemplateFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultDisputeTemplate, value: Double): Double = minOf(value, cap)
}

data class PercentDefaultDisputeTemplateAdjuster(var percent: Double) : DefaultDisputeTemplateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultDisputeTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDefaultDisputeTemplateAdjuster(var step: Double) : DefaultDisputeTemplateFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultDisputeTemplate, value: Double): Double = roundTo(value, step)
}

data class MinimumDefaultDisputeTemplateAdjuster(var minimum: Double) : DefaultDisputeTemplateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultDisputeTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the default dispute template steps in order and keeps a trace of each. */
data class DefaultDisputeTemplateEngine(
    var steps: MutableList<DefaultDisputeTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultDisputeTemplate): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultDisputeTemplateFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultDisputeTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultDisputeTemplateEngine(flags: Registry): DefaultDisputeTemplateEngine {
    return DefaultDisputeTemplateEngine(
        steps = mutableListOf<DefaultDisputeTemplateFactor>(
            DefaultDisputeTemplateSequenceOffsetAdjuster(0.63),
            FlaggedDefaultDisputeTemplateAdjuster(flags, "default-dispute-template-weighted", 6.5),
            CapDefaultDisputeTemplateAdjuster(31942.0),
            PercentDefaultDisputeTemplateAdjuster(-4.5),
            RoundingDefaultDisputeTemplateAdjuster(5.0),
            MinimumDefaultDisputeTemplateAdjuster(40.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 46520.2,
    )
}
