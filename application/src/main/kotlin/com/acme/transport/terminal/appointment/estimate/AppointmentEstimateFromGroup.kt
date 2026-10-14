package com.acme.transport.terminal.appointment.estimate

import com.acme.transport.terminal.dockdoor.ledger.DockDoorGroup
import com.acme.transport.terminal.dockdoor.ledger.classifyDockDoorPlan

fun summarizeDockDoorGroupsForEstimate(items: MutableList<DockDoorGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAppointmentEstimatesFromGroup(items: MutableList<DockDoorGroup>): MutableList<AppointmentEstimate> {
    val out = mutableListOf<AppointmentEstimate>()
    for (item in items) {
        val derived = newAppointmentEstimate(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyAppointmentEstimate(item: AppointmentEstimate): String = classifyDockDoorPlan(item.measuredValue())
