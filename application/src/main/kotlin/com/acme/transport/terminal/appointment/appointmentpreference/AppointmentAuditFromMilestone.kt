package com.acme.transport.terminal.appointment.appointmentpreference

import com.acme.transport.terminal.sortation.sortationpreference.SortationMilestone
import com.acme.transport.terminal.sortation.sortationpreference.classifySortationMilestone

fun summarizeSortationMilestonesForAudit(items: MutableList<SortationMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveAppointmentAuditsFromMilestone(items: MutableList<SortationMilestone>): MutableList<AppointmentAudit> {
    val out = mutableListOf<AppointmentAudit>()
    for (item in items) {
        val derived = newAppointmentAudit(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateAppointmentAuditExtra(item: AppointmentAudit): String {
    return classifySortationMilestone(item.effectiveValue())
}
