package com.acme.transport.terminal.crossdock.strictdockslot

import com.acme.transport.terminal.dockdoor.schedule.DefaultDockDoorSchedule
import com.acme.transport.terminal.dockdoor.schedule.pendingDockDoorDraftWeight

fun summarizeDefaultDockDoorSchedulesForAssignment(items: MutableList<DefaultDockDoorSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveScheduledCrossDockAssignmentsFromSchedule(items: MutableList<DefaultDockDoorSchedule>): MutableList<ScheduledCrossDockAssignment> {
    val out = mutableListOf<ScheduledCrossDockAssignment>()
    for (item in items) {
        val derived = newScheduledCrossDockAssignment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeScheduledCrossDockAssignment(item: ScheduledCrossDockAssignment): Double {
    return pendingDockDoorDraftWeight(2, true)
}
