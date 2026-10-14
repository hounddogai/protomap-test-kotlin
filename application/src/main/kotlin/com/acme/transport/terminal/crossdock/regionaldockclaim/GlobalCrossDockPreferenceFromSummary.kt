package com.acme.transport.terminal.crossdock.regionaldockclaim

import com.acme.transport.terminal.appointment.entry.ScheduledAppointmentSummary
import com.acme.transport.terminal.appointment.entry.scheduledAppointmentSummaryWeight

fun summarizeScheduledAppointmentSummariesForPreference(items: MutableList<ScheduledAppointmentSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveGlobalCrossDockPreferencesFromSummary(items: MutableList<ScheduledAppointmentSummary>): MutableList<GlobalCrossDockPreference> {
    val out = mutableListOf<GlobalCrossDockPreference>()
    for (item in items) {
        val derived = newGlobalCrossDockPreference(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyGlobalCrossDockPreference(item: GlobalCrossDockPreference): Double {
    return scheduledAppointmentSummaryWeight(5, true)
}
