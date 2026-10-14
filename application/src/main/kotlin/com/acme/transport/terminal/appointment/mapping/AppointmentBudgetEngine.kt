package com.acme.transport.terminal.appointment.mapping

import com.acme.transport.kit.numbers.roundTo

/** One step of the appointment budget calculation. */
interface AppointmentBudgetStep {
    fun describe(): String
    fun adjust(item: AppointmentBudget, value: Double): Double
}

data class PercentAppointmentBudgetAdjuster(var percent: Double) : AppointmentBudgetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AppointmentBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class AppointmentBudgetCapacityOffsetAdjuster(var weight: Double) : AppointmentBudgetStep {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: AppointmentBudget, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class TieredAppointmentBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AppointmentBudgetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AppointmentBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingAppointmentBudgetAdjuster(var step: Double) : AppointmentBudgetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AppointmentBudget, value: Double): Double = roundTo(value, step)
}

/** Runs the appointment budget steps in order and keeps a trace of each. */
data class AppointmentBudgetEngine(
    var steps: MutableList<AppointmentBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AppointmentBudget): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AppointmentBudgetStep = steps[index]
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

    fun evaluateAll(items: MutableList<AppointmentBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAppointmentBudgetEngine(): AppointmentBudgetEngine {
    return AppointmentBudgetEngine(
        steps = mutableListOf<AppointmentBudgetStep>(
            PercentAppointmentBudgetAdjuster(21.0),
            AppointmentBudgetCapacityOffsetAdjuster(0.78),
            TieredAppointmentBudgetAdjuster(
                mutableListOf<Double>(3155.0, 7491.0),
                mutableListOf<Double>(1.01, 0.93),
            ),
            RoundingAppointmentBudgetAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 75271.3,
    )
}
