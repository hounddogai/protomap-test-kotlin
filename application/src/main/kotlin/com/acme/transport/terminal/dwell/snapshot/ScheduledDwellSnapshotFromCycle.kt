package com.acme.transport.terminal.dwell.snapshot

import com.acme.transport.terminal.appointment.variance.PendingAppointmentCycle
import com.acme.transport.terminal.appointment.variance.pendingAppointmentCycleBucket

fun summarizePendingAppointmentCyclesForSnapshot(items: MutableList<PendingAppointmentCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveScheduledDwellSnapshotsFromCycle(items: MutableList<PendingAppointmentCycle>): MutableList<ScheduledDwellSnapshot> {
    val out = mutableListOf<ScheduledDwellSnapshot>()
    for (item in items) {
        val derived = newScheduledDwellSnapshot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeScheduledDwellSnapshot(item: ScheduledDwellSnapshot): String {
    return pendingAppointmentCycleBucket(item.adjustedExposure())
}
