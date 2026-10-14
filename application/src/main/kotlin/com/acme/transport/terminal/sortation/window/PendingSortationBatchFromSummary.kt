package com.acme.transport.terminal.sortation.window

import com.acme.transport.terminal.appointment.entry.ScheduledAppointmentSummary
import com.acme.transport.terminal.appointment.entry.classifyScheduledAppointmentSummary

fun summarizeScheduledAppointmentSummariesForBatch(items: MutableList<ScheduledAppointmentSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun derivePendingSortationBatchesFromSummary(items: MutableList<ScheduledAppointmentSummary>): MutableList<PendingSortationBatch> {
    val out = mutableListOf<PendingSortationBatch>()
    for (item in items) {
        val derived = newPendingSortationBatch(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignPendingSortationBatch(item: PendingSortationBatch): String {
    return classifyScheduledAppointmentSummary(item.effectiveExposure())
}
