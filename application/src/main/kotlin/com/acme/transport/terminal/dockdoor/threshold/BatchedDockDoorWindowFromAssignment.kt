package com.acme.transport.terminal.dockdoor.threshold

import com.acme.transport.terminal.crossdock.strictdockslot.ScheduledCrossDockAssignment
import com.acme.transport.terminal.crossdock.strictdockslot.scheduledCrossDockAssignmentWeight

fun summarizeScheduledCrossDockAssignmentsForWindow(items: MutableList<ScheduledCrossDockAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveBatchedDockDoorWindowsFromAssignment(items: MutableList<ScheduledCrossDockAssignment>): MutableList<BatchedDockDoorWindow> {
    val out = mutableListOf<BatchedDockDoorWindow>()
    for (item in items) {
        val derived = newBatchedDockDoorWindow(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planBatchedDockDoorWindow(item: BatchedDockDoorWindow): Double = scheduledCrossDockAssignmentWeight(8, true)
