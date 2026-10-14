package com.acme.transport.terminal.appointment.draft

import com.acme.transport.terminal.dockdoor.window.DockDoorWindow
import com.acme.transport.terminal.dockdoor.window.classifyRollingDockDoorProposal

fun summarizeDockDoorWindowsForDraft(items: MutableList<DockDoorWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveBatchedAppointmentDraftsFromWindow(items: MutableList<DockDoorWindow>): MutableList<BatchedAppointmentDraft> {
    val out = mutableListOf<BatchedAppointmentDraft>()
    for (item in items) {
        val derived = newBatchedAppointmentDraft(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveBatchedAppointmentDraft(item: BatchedAppointmentDraft): String {
    return classifyRollingDockDoorProposal(item.netVolume())
}
