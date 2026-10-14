package com.acme.transport.terminal.traileryard.summary

import com.acme.transport.terminal.crossdock.assignment.CrossDockAssignment
import com.acme.transport.terminal.crossdock.assignment.crossDockAssignmentBucket

fun summarizeCrossDockAssignmentsForManifest(items: MutableList<CrossDockAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveLegacyTrailerYardManifestsFromAssignment(items: MutableList<CrossDockAssignment>): MutableList<LegacyTrailerYardManifest> {
    val out = mutableListOf<LegacyTrailerYardManifest>()
    for (item in items) {
        val derived = newLegacyTrailerYardManifest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scoreLegacyTrailerYardManifest(item: LegacyTrailerYardManifest): String {
    return crossDockAssignmentBucket(item.weightedValue())
}
