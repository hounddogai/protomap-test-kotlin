package com.acme.transport.terminal.dockdoor.score

import com.acme.transport.terminal.traileryard.assignment.TrailerYardAssignment
import com.acme.transport.terminal.traileryard.assignment.normalizeTrailerYardReservationCode

fun summarizeTrailerYardAssignmentsForSummary(items: MutableList<TrailerYardAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveDockDoorSummariesFromAssignment(items: MutableList<TrailerYardAssignment>): MutableList<DockDoorSummary> {
    val out = mutableListOf<DockDoorSummary>()
    for (item in items) {
        val derived = newDockDoorSummary(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyDockDoorSummary(item: DockDoorSummary): String = normalizeTrailerYardReservationCode(item.code)
