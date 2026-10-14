package com.acme.transport.terminal.appointment.appointmenttemplate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped appointment template calculation. */
interface CappedAppointmentTemplateStep {
    fun describe(): String
    fun adjust(item: CappedAppointmentTemplate, value: Double): Double
}

data class CappedAppointmentTemplateThresholdOffsetAdjuster(
    var weight: Double,
) : CappedAppointmentTemplateStep {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: CappedAppointmentTemplate, value: Double): Double = value + item.threshold * weight
}

data class FlaggedCappedAppointmentTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedAppointmentTemplateStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedAppointmentTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentCappedAppointmentTemplateAdjuster(var percent: Double) : CappedAppointmentTemplateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedAppointmentTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingCappedAppointmentTemplateAdjuster(var step: Double) : CappedAppointmentTemplateStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedAppointmentTemplate, value: Double): Double = roundTo(value, step)
}

data class LaneCappedAppointmentTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedAppointmentTemplateStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: CappedAppointmentTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapCappedAppointmentTemplateAdjuster(var cap: Double) : CappedAppointmentTemplateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedAppointmentTemplate, value: Double): Double = minOf(value, cap)
}

/** Runs the capped appointment template steps in order and keeps a trace of each. */
data class CappedAppointmentTemplateEngine(
    var steps: MutableList<CappedAppointmentTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedAppointmentTemplate): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedAppointmentTemplateStep = steps[index]
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

    fun evaluateAll(items: MutableList<CappedAppointmentTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedAppointmentTemplateEngine(flags: Registry): CappedAppointmentTemplateEngine {
    return CappedAppointmentTemplateEngine(
        steps = mutableListOf<CappedAppointmentTemplateStep>(
            CappedAppointmentTemplateThresholdOffsetAdjuster(1.16),
            FlaggedCappedAppointmentTemplateAdjuster(flags, "capped-appointment-template-pending", -9.2),
            PercentCappedAppointmentTemplateAdjuster(-3.1),
            RoundingCappedAppointmentTemplateAdjuster(0.05),
            LaneCappedAppointmentTemplateAdjuster(
                mutableMapOf<String, Double>("retail" to 0.81, "online" to 1.21, "north" to 0.92),
            ),
            CapCappedAppointmentTemplateAdjuster(40138.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 73116.4,
    )
}
