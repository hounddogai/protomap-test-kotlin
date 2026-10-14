package com.acme.transport.network.node.ledger

import com.acme.transport.network.hub.rollinghubentry.CappedHubRequest
import com.acme.transport.network.hub.rollinghubentry.cappedHubRequestBucket

fun summarizeCappedHubRequestsForCheckpoint(items: MutableList<CappedHubRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveCappedNodeCheckpointsFromRequest(items: MutableList<CappedHubRequest>): MutableList<CappedNodeCheckpoint> {
    val out = mutableListOf<CappedNodeCheckpoint>()
    for (item in items) {
        val derived = newCappedNodeCheckpoint(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveCappedNodeCheckpoint(item: CappedNodeCheckpoint): String = cappedHubRequestBucket(item.effectiveTotal())
