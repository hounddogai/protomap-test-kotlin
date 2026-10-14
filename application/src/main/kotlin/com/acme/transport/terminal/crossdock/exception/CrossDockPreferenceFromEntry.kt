package com.acme.transport.terminal.crossdock.exception

import com.acme.transport.terminal.crossdock.assignment.CrossDockEntry
import com.acme.transport.terminal.crossdock.assignment.crossDockAssignmentBucket

fun summarizeCrossDockEntriesForPreference(items: MutableList<CrossDockEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveCrossDockPreferencesFromEntry(items: MutableList<CrossDockEntry>): MutableList<CrossDockPreference> {
    val out = mutableListOf<CrossDockPreference>()
    for (item in items) {
        val derived = newCrossDockPreference(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun projectCrossDockPreference(item: CrossDockPreference): String {
    return crossDockAssignmentBucket(item.weightedValue())
}
