package com.acme.transport.terminal.sortation.tier

import com.acme.transport.terminal.appointment.appointmentpreference.AppointmentManifest
import com.acme.transport.terminal.appointment.appointmentpreference.normalizeAppointmentDigestCode

fun summarizeAppointmentManifestsForBatch(items: MutableList<AppointmentManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveSortationBatchesFromManifest(items: MutableList<AppointmentManifest>): MutableList<SortationBatch> {
    val out = mutableListOf<SortationBatch>()
    for (item in items) {
        val derived = newSortationBatch(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileSortationBatch(item: SortationBatch): String = normalizeAppointmentDigestCode(item.code)
