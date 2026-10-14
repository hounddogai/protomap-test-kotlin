package com.acme.transport.terminal.dockdoor.snapshot

import com.acme.transport.terminal.appointment.appointmentpreference.AppointmentDigest
import com.acme.transport.terminal.appointment.appointmentpreference.classifyCappedAppointmentPreference

fun summarizeAppointmentDigestsForException(items: MutableList<AppointmentDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveFallbackDockDoorExceptionsFromDigest(items: MutableList<AppointmentDigest>): MutableList<FallbackDockDoorException> {
    val out = mutableListOf<FallbackDockDoorException>()
    for (item in items) {
        val derived = newFallbackDockDoorException(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeFallbackDockDoorException(item: FallbackDockDoorException): String {
    return classifyCappedAppointmentPreference(item.projectedVolume())
}
