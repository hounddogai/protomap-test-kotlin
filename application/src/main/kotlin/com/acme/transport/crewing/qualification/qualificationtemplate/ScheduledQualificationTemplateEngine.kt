package com.acme.transport.crewing.qualification.qualificationtemplate

import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled qualification template calculation. */
interface ScheduledQualificationTemplateModifier {
    fun describe(): String
    fun adjust(item: ScheduledQualificationTemplate, value: Double): Double
}

data class MinimumScheduledQualificationTemplateAdjuster(
    var minimum: Double,
) : ScheduledQualificationTemplateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledQualificationTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingScheduledQualificationTemplateAdjuster(
    var step: Double,
) : ScheduledQualificationTemplateModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledQualificationTemplate, value: Double): Double = roundTo(value, step)
}

data class ScheduledQualificationTemplatePalletsOffsetAdjuster(
    var weight: Double,
) : ScheduledQualificationTemplateModifier {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: ScheduledQualificationTemplate, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class TieredScheduledQualificationTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledQualificationTemplateModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledQualificationTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scheduled qualification template steps in order and keeps a trace of each. */
data class ScheduledQualificationTemplateEngine(
    var steps: MutableList<ScheduledQualificationTemplateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledQualificationTemplate): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledQualificationTemplateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledQualificationTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledQualificationTemplateEngine(): ScheduledQualificationTemplateEngine {
    return ScheduledQualificationTemplateEngine(
        steps = mutableListOf<ScheduledQualificationTemplateModifier>(
            MinimumScheduledQualificationTemplateAdjuster(4.0),
            RoundingScheduledQualificationTemplateAdjuster(5.0),
            ScheduledQualificationTemplatePalletsOffsetAdjuster(1.9),
            TieredScheduledQualificationTemplateAdjuster(
                mutableListOf<Double>(89.0, 4224.0, 4513.0, 8808.0),
                mutableListOf<Double>(0.83, 1.2, 0.88, 0.81),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 61770.2,
    )
}
