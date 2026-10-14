package com.acme.transport.terminal.dockdoor.schedule

import com.acme.transport.terminal.appointment.adaptiveappointmentmapping.WeightedAppointmentMapping
import com.acme.transport.terminal.appointment.adaptiveappointmentmapping.classifyWeightedAppointmentMapping

fun summarizeWeightedAppointmentMappingsForDraft(items: MutableList<WeightedAppointmentMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun derivePendingDockDoorDraftsFromMapping(items: MutableList<WeightedAppointmentMapping>): MutableList<PendingDockDoorDraft> {
    val out = mutableListOf<PendingDockDoorDraft>()
    for (item in items) {
        val derived = newPendingDockDoorDraft(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settlePendingDockDoorDraft(item: PendingDockDoorDraft): String {
    return classifyWeightedAppointmentMapping(item.projectedExposure())
}
