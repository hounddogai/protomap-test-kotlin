package com.acme.transport.terminal.appointment.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global appointment budget calculation. */
interface GlobalAppointmentBudgetModifier {
    fun describe(): String
    fun adjust(item: GlobalAppointmentBudget, value: Double): Double
}

data class RoundingGlobalAppointmentBudgetAdjuster(var step: Double) : GlobalAppointmentBudgetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalAppointmentBudget, value: Double): Double = roundTo(value, step)
}

data class PercentGlobalAppointmentBudgetAdjuster(var percent: Double) : GlobalAppointmentBudgetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalAppointmentBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedGlobalAppointmentBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalAppointmentBudgetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalAppointmentBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the global appointment budget steps in order and keeps a trace of each. */
data class GlobalAppointmentBudgetEngine(
    var steps: MutableList<GlobalAppointmentBudgetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalAppointmentBudget): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalAppointmentBudgetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalAppointmentBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalAppointmentBudgetEngine(flags: Registry): GlobalAppointmentBudgetEngine {
    return GlobalAppointmentBudgetEngine(
        steps = mutableListOf<GlobalAppointmentBudgetModifier>(
            RoundingGlobalAppointmentBudgetAdjuster(5.0),
            PercentGlobalAppointmentBudgetAdjuster(-22.5),
            FlaggedGlobalAppointmentBudgetAdjuster(flags, "global-appointment-budget-primary", -1.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 94292.2,
    )
}
