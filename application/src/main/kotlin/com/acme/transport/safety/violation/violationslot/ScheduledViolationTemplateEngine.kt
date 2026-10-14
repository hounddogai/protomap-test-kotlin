package com.acme.transport.safety.violation.violationslot

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled violation template calculation. */
interface ScheduledViolationTemplateStep {
    fun describe(): String
    fun adjust(item: ScheduledViolationTemplate, value: Double): Double
}

data class RoundingScheduledViolationTemplateAdjuster(var step: Double) : ScheduledViolationTemplateStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledViolationTemplate, value: Double): Double = roundTo(value, step)
}

data class TieredScheduledViolationTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledViolationTemplateStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledViolationTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameScheduledViolationTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledViolationTemplateStep {
    override fun describe(): String = "by name"

    override fun adjust(item: ScheduledViolationTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scheduled violation template steps in order and keeps a trace of each. */
data class ScheduledViolationTemplateEngine(
    var steps: MutableList<ScheduledViolationTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledViolationTemplate): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledViolationTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledViolationTemplateEngine(): ScheduledViolationTemplateEngine {
    return ScheduledViolationTemplateEngine(
        steps = mutableListOf<ScheduledViolationTemplateStep>(
            RoundingScheduledViolationTemplateAdjuster(0.05),
            TieredScheduledViolationTemplateAdjuster(
                mutableListOf<Double>(1948.0, 3149.0, 4059.0, 7668.0),
                mutableListOf<Double>(1.06, 1.02, 0.85, 0.87),
            ),
            NameScheduledViolationTemplateAdjuster(
                mutableMapOf<String, Double>("north" to 1.25, "south" to 0.99),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 96084.7,
    )
}
