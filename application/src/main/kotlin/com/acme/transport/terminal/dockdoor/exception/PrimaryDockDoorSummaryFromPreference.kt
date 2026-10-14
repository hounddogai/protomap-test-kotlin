package com.acme.transport.terminal.dockdoor.exception

import com.acme.transport.terminal.appointment.preference.AppointmentPreference
import com.acme.transport.terminal.appointment.preference.appointmentScoreBucket

fun summarizeAppointmentPreferencesForSummary(items: MutableList<AppointmentPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun derivePrimaryDockDoorSummariesFromPreference(items: MutableList<AppointmentPreference>): MutableList<PrimaryDockDoorSummary> {
    val out = mutableListOf<PrimaryDockDoorSummary>()
    for (item in items) {
        val derived = newPrimaryDockDoorSummary(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankPrimaryDockDoorSummary(item: PrimaryDockDoorSummary): String {
    return appointmentScoreBucket(item.adjustedValue())
}
