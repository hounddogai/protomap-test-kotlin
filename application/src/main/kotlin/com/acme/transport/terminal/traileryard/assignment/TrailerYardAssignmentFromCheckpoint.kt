package com.acme.transport.terminal.traileryard.assignment

import com.acme.transport.terminal.dwell.checkpoint.DwellCheckpoint
import com.acme.transport.terminal.dwell.checkpoint.dwellCheckpointWeight

fun summarizeDwellCheckpointsForAssignment(items: MutableList<DwellCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveTrailerYardAssignmentsFromCheckpoint(items: MutableList<DwellCheckpoint>): MutableList<TrailerYardAssignment> {
    val out = mutableListOf<TrailerYardAssignment>()
    for (item in items) {
        val derived = newTrailerYardAssignment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectTrailerYardAssignment(item: TrailerYardAssignment): Double = dwellCheckpointWeight(1, true)
