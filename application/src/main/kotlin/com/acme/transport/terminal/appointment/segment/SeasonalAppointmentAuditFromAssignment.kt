package com.acme.transport.terminal.appointment.segment

import com.acme.transport.terminal.crossdock.assignment.CrossDockAssignment
import com.acme.transport.terminal.crossdock.assignment.normalizeCrossDockAssignmentCode

fun summarizeCrossDockAssignmentsForAudit(items: MutableList<CrossDockAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveSeasonalAppointmentAuditsFromAssignment(items: MutableList<CrossDockAssignment>): MutableList<SeasonalAppointmentAudit> {
    val out = mutableListOf<SeasonalAppointmentAudit>()
    for (item in items) {
        val derived = newSeasonalAppointmentAudit(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun settleSeasonalAppointmentAudit(item: SeasonalAppointmentAudit): String {
    return normalizeCrossDockAssignmentCode(item.code)
}
