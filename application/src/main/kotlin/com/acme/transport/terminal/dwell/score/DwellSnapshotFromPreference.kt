package com.acme.transport.terminal.dwell.score

import com.acme.transport.terminal.appointment.appointmentpreference.CappedAppointmentPreference
import com.acme.transport.terminal.appointment.appointmentpreference.classifyCappedAppointmentPreference

fun summarizeCappedAppointmentPreferencesForSnapshot(items: MutableList<CappedAppointmentPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveDwellSnapshotsFromPreference(items: MutableList<CappedAppointmentPreference>): MutableList<DwellSnapshot> {
    val out = mutableListOf<DwellSnapshot>()
    for (item in items) {
        val derived = newDwellSnapshot(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitDwellSnapshot(item: DwellSnapshot): String = classifyCappedAppointmentPreference(item.projectedLoad())
