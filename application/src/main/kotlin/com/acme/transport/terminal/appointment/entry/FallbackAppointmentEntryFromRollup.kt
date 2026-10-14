package com.acme.transport.terminal.appointment.entry

import com.acme.transport.terminal.sortation.rollup.ManualSortationRollup
import com.acme.transport.terminal.sortation.rollup.classifyGlobalSortationDraft

fun summarizeManualSortationRollupsForEntry(items: MutableList<ManualSortationRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveFallbackAppointmentEntriesFromRollup(items: MutableList<ManualSortationRollup>): MutableList<FallbackAppointmentEntry> {
    val out = mutableListOf<FallbackAppointmentEntry>()
    for (item in items) {
        val derived = newFallbackAppointmentEntry(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitFallbackAppointmentEntry(item: FallbackAppointmentEntry): String {
    return classifyGlobalSortationDraft(item.projectedValue())
}
