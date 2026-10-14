package com.acme.transport.terminal.appointment.seasonalappointmentvariance

import com.acme.transport.terminal.appointment.ledger.PrimaryAppointmentPreference
import com.acme.transport.terminal.appointment.ledger.classifySeasonalAppointmentOverride

fun summarizePrimaryAppointmentPreferencesForVariance(items: MutableList<PrimaryAppointmentPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun derivePendingAppointmentVariancesFromPreference(items: MutableList<PrimaryAppointmentPreference>): MutableList<PendingAppointmentVariance> {
    val out = mutableListOf<PendingAppointmentVariance>()
    for (item in items) {
        val derived = newPendingAppointmentVariance(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizePendingAppointmentVariance(item: PendingAppointmentVariance): String {
    return classifySeasonalAppointmentOverride(item.measuredValue())
}
