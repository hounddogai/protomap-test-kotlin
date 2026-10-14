package com.acme.transport.terminal.appointment.metric

import com.acme.transport.kit.flags.Registry

/** One step of the appointment plan calculation. */
interface AppointmentPlanModifier {
    fun describe(): String
    fun adjust(item: AppointmentPlan, value: Double): Double
}

data class PercentAppointmentPlanAdjuster(var percent: Double) : AppointmentPlanModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AppointmentPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SourceAppointmentPlanAdjuster(var factors: MutableMap<String, Double>) : AppointmentPlanModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: AppointmentPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredAppointmentPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AppointmentPlanModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AppointmentPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedAppointmentPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AppointmentPlanModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AppointmentPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumAppointmentPlanAdjuster(var minimum: Double) : AppointmentPlanModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AppointmentPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the appointment plan steps in order and keeps a trace of each. */
data class AppointmentPlanEngine(
    var steps: MutableList<AppointmentPlanModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AppointmentPlan): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AppointmentPlanModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AppointmentPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAppointmentPlanEngine(flags: Registry): AppointmentPlanEngine {
    return AppointmentPlanEngine(
        steps = mutableListOf<AppointmentPlanModifier>(
            PercentAppointmentPlanAdjuster(-23.7),
            SourceAppointmentPlanAdjuster(
                mutableMapOf<String, Double>("north" to 1.07, "east" to 0.92, "retail" to 1.34),
            ),
            TieredAppointmentPlanAdjuster(
                mutableListOf<Double>(2223.0, 6777.0, 7579.0, 8556.0),
                mutableListOf<Double>(0.92, 0.94, 1.18, 0.95),
            ),
            FlaggedAppointmentPlanAdjuster(flags, "appointment-plan-strict", -4.4),
            MinimumAppointmentPlanAdjuster(3.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 17405.1,
    )
}
