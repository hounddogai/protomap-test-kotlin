package com.acme.transport.terminal.sortation.metric

import com.acme.transport.terminal.appointment.threshold.AppointmentThreshold
import com.acme.transport.terminal.appointment.threshold.classifyAppointmentThreshold

fun summarizeAppointmentThresholdsForClaim(items: MutableList<AppointmentThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveScheduledSortationClaimsFromThreshold(items: MutableList<AppointmentThreshold>): MutableList<ScheduledSortationClaim> {
    val out = mutableListOf<ScheduledSortationClaim>()
    for (item in items) {
        val derived = newScheduledSortationClaim(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun applyScheduledSortationClaim(item: ScheduledSortationClaim): String {
    return classifyAppointmentThreshold(item.weightedLoad())
}
