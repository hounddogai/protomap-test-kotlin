package com.acme.transport.terminal.appointment.mapping

import com.acme.transport.terminal.traileryard.assignment.TrailerYardDraft
import com.acme.transport.terminal.traileryard.assignment.classifyTrailerYardException

fun summarizeTrailerYardDraftsForBudget(items: MutableList<TrailerYardDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAppointmentBudgetsFromDraft(items: MutableList<TrailerYardDraft>): MutableList<AppointmentBudget> {
    val out = mutableListOf<AppointmentBudget>()
    for (item in items) {
        val derived = newAppointmentBudget(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun matchAppointmentBudget(item: AppointmentBudget): String = classifyTrailerYardException(item.netTotal())
