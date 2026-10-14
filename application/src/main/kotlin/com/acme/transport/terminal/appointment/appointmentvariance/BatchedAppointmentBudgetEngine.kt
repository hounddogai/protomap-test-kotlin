package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched appointment budget calculation. */
interface BatchedAppointmentBudgetFactor {
    fun describe(): String
    fun adjust(item: BatchedAppointmentBudget, value: Double): Double
}

data class MinimumBatchedAppointmentBudgetAdjuster(var minimum: Double) : BatchedAppointmentBudgetFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedAppointmentBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedBatchedAppointmentBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedAppointmentBudgetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedAppointmentBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentBatchedAppointmentBudgetAdjuster(var percent: Double) : BatchedAppointmentBudgetFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedAppointmentBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingBatchedAppointmentBudgetAdjuster(var step: Double) : BatchedAppointmentBudgetFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedAppointmentBudget, value: Double): Double = roundTo(value, step)
}

/** Runs the batched appointment budget steps in order and keeps a trace of each. */
data class BatchedAppointmentBudgetEngine(
    var steps: MutableList<BatchedAppointmentBudgetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedAppointmentBudget): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedAppointmentBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedAppointmentBudgetEngine(flags: Registry): BatchedAppointmentBudgetEngine {
    return BatchedAppointmentBudgetEngine(
        steps = mutableListOf<BatchedAppointmentBudgetFactor>(
            MinimumBatchedAppointmentBudgetAdjuster(9.1),
            FlaggedBatchedAppointmentBudgetAdjuster(flags, "batched-appointment-budget-regional", 3.8),
            PercentBatchedAppointmentBudgetAdjuster(-18.6),
            RoundingBatchedAppointmentBudgetAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 46090.6,
    )
}
