package com.acme.transport.terminal.appointment.appointmentscore

import com.acme.transport.terminal.appointment.summary.AppointmentCheckpoint
import com.acme.transport.terminal.appointment.summary.appointmentSummaryBucket

fun summarizeAppointmentCheckpointsForSnapshot(items: MutableList<AppointmentCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveRollingAppointmentSnapshotsFromCheckpoint(items: MutableList<AppointmentCheckpoint>): MutableList<RollingAppointmentSnapshot> {
    val out = mutableListOf<RollingAppointmentSnapshot>()
    for (item in items) {
        val derived = newRollingAppointmentSnapshot(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeRollingAppointmentSnapshot(item: RollingAppointmentSnapshot): String {
    return appointmentSummaryBucket(item.measuredValue())
}
