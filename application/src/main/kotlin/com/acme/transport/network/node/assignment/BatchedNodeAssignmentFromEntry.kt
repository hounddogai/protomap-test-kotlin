package com.acme.transport.network.node.assignment

import com.acme.transport.network.zone.entry.ZoneEntry
import com.acme.transport.network.zone.entry.adaptiveZoneAllocationWeight

fun summarizeZoneEntriesForAssignment(items: MutableList<ZoneEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveBatchedNodeAssignmentsFromEntry(items: MutableList<ZoneEntry>): MutableList<BatchedNodeAssignment> {
    val out = mutableListOf<BatchedNodeAssignment>()
    for (item in items) {
        val derived = newBatchedNodeAssignment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun rankBatchedNodeAssignment(item: BatchedNodeAssignment): Double = adaptiveZoneAllocationWeight(5, true)
