package com.acme.transport.network.zone.reservation

import com.acme.transport.network.node.ledger.CappedNodeCheckpoint
import com.acme.transport.network.node.ledger.fallbackNodeEstimateBucket

fun summarizeCappedNodeCheckpointsForSegment(items: MutableList<CappedNodeCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveBatchedZoneSegmentsFromCheckpoint(items: MutableList<CappedNodeCheckpoint>): MutableList<BatchedZoneSegment> {
    val out = mutableListOf<BatchedZoneSegment>()
    for (item in items) {
        val derived = newBatchedZoneSegment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankBatchedZoneSegment(item: BatchedZoneSegment): String = fallbackNodeEstimateBucket(item.netTotal())
