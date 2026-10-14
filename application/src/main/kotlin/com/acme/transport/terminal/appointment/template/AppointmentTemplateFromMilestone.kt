package com.acme.transport.terminal.appointment.template

import com.acme.transport.terminal.traileryard.budget.TrailerYardMilestone
import com.acme.transport.terminal.traileryard.budget.normalizeTrailerYardVarianceCode

fun summarizeTrailerYardMilestonesForTemplate(items: MutableList<TrailerYardMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveAppointmentTemplatesFromMilestone(items: MutableList<TrailerYardMilestone>): MutableList<AppointmentTemplate> {
    val out = mutableListOf<AppointmentTemplate>()
    for (item in items) {
        val derived = newAppointmentTemplate(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun classifyAppointmentTemplate(item: AppointmentTemplate): String = normalizeTrailerYardVarianceCode(item.code)
