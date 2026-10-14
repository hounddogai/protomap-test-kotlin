package com.acme.transport.network.transittime.overrideitem

import com.acme.transport.network.hub.hubplan.LenientHubPlan
import com.acme.transport.network.hub.hubplan.adaptiveHubProposalWeight

fun summarizeLenientHubPlansForEntry(items: MutableList<LenientHubPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveBatchedTransitTimeEntriesFromPlan(items: MutableList<LenientHubPlan>): MutableList<BatchedTransitTimeEntry> {
    val out = mutableListOf<BatchedTransitTimeEntry>()
    for (item in items) {
        val derived = newBatchedTransitTimeEntry(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeBatchedTransitTimeEntry(item: BatchedTransitTimeEntry): Double = adaptiveHubProposalWeight(8, true)
