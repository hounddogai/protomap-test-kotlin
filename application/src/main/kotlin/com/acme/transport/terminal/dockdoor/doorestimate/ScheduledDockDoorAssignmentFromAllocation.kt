package com.acme.transport.terminal.dockdoor.doorestimate

import com.acme.transport.terminal.appointment.appointmentallocation.CappedAppointmentAllocation
import com.acme.transport.terminal.appointment.appointmentallocation.strictAppointmentSummaryWeight

fun summarizeCappedAppointmentAllocationsForAssignment(items: MutableList<CappedAppointmentAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveScheduledDockDoorAssignmentsFromAllocation(items: MutableList<CappedAppointmentAllocation>): MutableList<ScheduledDockDoorAssignment> {
    val out = mutableListOf<ScheduledDockDoorAssignment>()
    for (item in items) {
        val derived = newScheduledDockDoorAssignment(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleScheduledDockDoorAssignment(item: ScheduledDockDoorAssignment): Double {
    return strictAppointmentSummaryWeight(5, true)
}
