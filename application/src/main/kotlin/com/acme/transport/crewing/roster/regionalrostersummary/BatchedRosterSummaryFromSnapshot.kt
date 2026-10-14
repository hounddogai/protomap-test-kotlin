package com.acme.transport.crewing.roster.regionalrostersummary

import com.acme.transport.crewing.swap.snapshot.PendingSwapSnapshot
import com.acme.transport.crewing.swap.snapshot.classifySwapRequest

fun summarizePendingSwapSnapshotsForSummary(items: MutableList<PendingSwapSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveBatchedRosterSummariesFromSnapshot(items: MutableList<PendingSwapSnapshot>): MutableList<BatchedRosterSummary> {
    val out = mutableListOf<BatchedRosterSummary>()
    for (item in items) {
        val derived = newBatchedRosterSummary(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateBatchedRosterSummary(item: BatchedRosterSummary): String = classifySwapRequest(item.effectiveLoad())
