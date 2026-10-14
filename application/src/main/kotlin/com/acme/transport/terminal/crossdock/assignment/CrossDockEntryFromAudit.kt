package com.acme.transport.terminal.crossdock.assignment

import com.acme.transport.terminal.crossdock.variance.CrossDockAudit
import com.acme.transport.terminal.crossdock.variance.classifyCrossDockAudit

fun summarizeCrossDockAuditsForEntry(items: MutableList<CrossDockAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveCrossDockEntriesFromAudit(items: MutableList<CrossDockAudit>): MutableList<CrossDockEntry> {
    val out = mutableListOf<CrossDockEntry>()
    for (item in items) {
        val derived = newCrossDockEntry(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeCrossDockEntry(item: CrossDockEntry): String = classifyCrossDockAudit(item.weightedLoad())
