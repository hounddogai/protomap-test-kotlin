package com.acme.transport.terminal.appointment.template

import com.acme.transport.kit.numbers.roundTo

/** One step of the appointment template calculation. */
interface AppointmentTemplateStep {
    fun describe(): String
    fun adjust(item: AppointmentTemplate, value: Double): Double
}

data class PercentAppointmentTemplateAdjuster(var percent: Double) : AppointmentTemplateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AppointmentTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAppointmentTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AppointmentTemplateStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AppointmentTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingAppointmentTemplateAdjuster(var step: Double) : AppointmentTemplateStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AppointmentTemplate, value: Double): Double = roundTo(value, step)
}

data class CapAppointmentTemplateAdjuster(var cap: Double) : AppointmentTemplateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AppointmentTemplate, value: Double): Double = minOf(value, cap)
}

/** Runs the appointment template steps in order and keeps a trace of each. */
data class AppointmentTemplateEngine(
    var steps: MutableList<AppointmentTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AppointmentTemplate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AppointmentTemplateStep = steps[index]
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

    fun evaluateAll(items: MutableList<AppointmentTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAppointmentTemplateEngine(): AppointmentTemplateEngine {
    return AppointmentTemplateEngine(
        steps = mutableListOf<AppointmentTemplateStep>(
            PercentAppointmentTemplateAdjuster(-10.0),
            TieredAppointmentTemplateAdjuster(
                mutableListOf<Double>(1120.0, 5472.0),
                mutableListOf<Double>(1.19, 0.82),
            ),
            RoundingAppointmentTemplateAdjuster(0.5),
            CapAppointmentTemplateAdjuster(13932.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 45082.0,
    )
}
