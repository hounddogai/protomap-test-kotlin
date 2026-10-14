package com.acme.transport.terminal.appointment.estimate

import com.acme.transport.terminal.sortation.claim.SortationSnapshot
import com.acme.transport.terminal.sortation.claim.sortationSnapshotBucket

fun summarizeSortationSnapshotsForSchedule(items: MutableList<SortationSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveAppointmentSchedulesFromSnapshot(items: MutableList<SortationSnapshot>): MutableList<AppointmentSchedule> {
    val out = mutableListOf<AppointmentSchedule>()
    for (item in items) {
        val derived = newAppointmentSchedule(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifyAppointmentSchedule(item: AppointmentSchedule): String = sortationSnapshotBucket(item.weightedTotal())
