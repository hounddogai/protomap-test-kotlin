package com.acme.transport.terminal.appointment.metric

import com.acme.transport.terminal.sortation.snapshot.SortationThreshold
import com.acme.transport.terminal.sortation.snapshot.weightedSortationSnapshotBucket

fun summarizeSortationThresholdsForAdjustment(items: MutableList<SortationThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveAppointmentAdjustmentsFromThreshold(items: MutableList<SortationThreshold>): MutableList<AppointmentAdjustment> {
    val out = mutableListOf<AppointmentAdjustment>()
    for (item in items) {
        val derived = newAppointmentAdjustment(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeAppointmentAdjustment(item: AppointmentAdjustment): String {
    return weightedSortationSnapshotBucket(item.weightedValue())
}
