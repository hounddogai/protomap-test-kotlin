package com.acme.transport.terminal.appointment.allocation

import com.acme.transport.terminal.dockdoor.exception.DockDoorException
import com.acme.transport.terminal.dockdoor.exception.classifyDockDoorEntry

fun summarizeDockDoorExceptionsForAllocation(items: MutableList<DockDoorException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveAppointmentAllocationsFromException(items: MutableList<DockDoorException>): MutableList<AppointmentAllocation> {
    val out = mutableListOf<AppointmentAllocation>()
    for (item in items) {
        val derived = newAppointmentAllocation(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateAppointmentAllocation(item: AppointmentAllocation): String = classifyDockDoorEntry(item.netExposure())
