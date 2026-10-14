package com.acme.transport.terminal.dockdoor.doorclaim

import com.acme.transport.terminal.appointment.regionalappointmentmapping.AppointmentRequest
import com.acme.transport.terminal.appointment.regionalappointmentmapping.normalizeAppointmentRequestCode

fun summarizeAppointmentRequestsForOverride(items: MutableList<AppointmentRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDockDoorOverridesFromRequest(items: MutableList<AppointmentRequest>): MutableList<DockDoorOverride> {
    val out = mutableListOf<DockDoorOverride>()
    for (item in items) {
        val derived = newDockDoorOverride(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun estimateDockDoorOverride(item: DockDoorOverride): String = normalizeAppointmentRequestCode(item.code)
