package com.acme.transport.terminal.appointment.metric

import com.acme.transport.kit.numbers.roundTo

/** One step of the pending appointment audit calculation. */
interface PendingAppointmentAuditStep {
    fun describe(): String
    fun adjust(item: PendingAppointmentAudit, value: Double): Double
}

data class MinimumPendingAppointmentAuditAdjuster(var minimum: Double) : PendingAppointmentAuditStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingAppointmentAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingPendingAppointmentAuditAdjuster(var step: Double) : PendingAppointmentAuditStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingAppointmentAudit, value: Double): Double = roundTo(value, step)
}

data class PendingAppointmentAuditAmountOffsetAdjuster(var weight: Double) : PendingAppointmentAuditStep {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: PendingAppointmentAudit, value: Double): Double = value + item.amount * weight
}

/** Runs the pending appointment audit steps in order and keeps a trace of each. */
data class PendingAppointmentAuditEngine(
    var steps: MutableList<PendingAppointmentAuditStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingAppointmentAudit): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingAppointmentAuditStep = steps[index]
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

    fun evaluateAll(items: MutableList<PendingAppointmentAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingAppointmentAuditEngine(): PendingAppointmentAuditEngine {
    return PendingAppointmentAuditEngine(
        steps = mutableListOf<PendingAppointmentAuditStep>(
            MinimumPendingAppointmentAuditAdjuster(14.1),
            RoundingPendingAppointmentAuditAdjuster(0.25),
            PendingAppointmentAuditAmountOffsetAdjuster(1.99),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 89410.8,
    )
}
