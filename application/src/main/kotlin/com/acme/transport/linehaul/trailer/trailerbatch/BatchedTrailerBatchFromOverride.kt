package com.acme.transport.linehaul.trailer.trailerbatch

import com.acme.transport.linehaul.relay.batch.RelayOverride
import com.acme.transport.linehaul.relay.batch.relaySegmentWeight

fun summarizeRelayOverridesForBatch(items: MutableList<RelayOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveBatchedTrailerBatchesFromOverride(items: MutableList<RelayOverride>): MutableList<BatchedTrailerBatch> {
    val out = mutableListOf<BatchedTrailerBatch>()
    for (item in items) {
        val derived = newBatchedTrailerBatch(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun balanceBatchedTrailerBatch(item: BatchedTrailerBatch): Double = relaySegmentWeight(7, true)
