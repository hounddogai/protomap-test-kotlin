package com.acme.transport.terminal.crossdock.baseline

import com.acme.transport.terminal.appointment.ledger.PrimaryAppointmentPreference
import com.acme.transport.terminal.appointment.ledger.classifySeasonalAppointmentOverride

fun summarizePrimaryAppointmentPreferencesForMilestone(items: MutableList<PrimaryAppointmentPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveCrossDockMilestonesFromPreference(items: MutableList<PrimaryAppointmentPreference>): MutableList<CrossDockMilestone> {
    val out = mutableListOf<CrossDockMilestone>()
    for (item in items) {
        val derived = newCrossDockMilestone(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankCrossDockMilestone(item: CrossDockMilestone): String {
    return classifySeasonalAppointmentOverride(item.netTotal())
}
