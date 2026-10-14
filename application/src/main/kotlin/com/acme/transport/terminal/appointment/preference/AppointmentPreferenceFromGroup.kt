package com.acme.transport.terminal.appointment.preference

import com.acme.transport.terminal.dwell.cycle.DwellGroup
import com.acme.transport.terminal.dwell.cycle.normalizeDwellAssignmentCode

fun summarizeDwellGroupsForPreference(items: MutableList<DwellGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveAppointmentPreferencesFromGroup(items: MutableList<DwellGroup>): MutableList<AppointmentPreference> {
    val out = mutableListOf<AppointmentPreference>()
    for (item in items) {
        val derived = newAppointmentPreference(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun settleAppointmentPreference(item: AppointmentPreference): String = normalizeDwellAssignmentCode(item.code)
