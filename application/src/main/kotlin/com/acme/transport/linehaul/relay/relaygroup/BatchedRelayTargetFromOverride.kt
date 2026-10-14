package com.acme.transport.linehaul.relay.relaygroup

import com.acme.transport.linehaul.trailer.traileroverride.BatchedTrailerOverride
import com.acme.transport.linehaul.trailer.traileroverride.tieredTrailerWindowWeight

fun summarizeBatchedTrailerOverridesForTarget(items: MutableList<BatchedTrailerOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveBatchedRelayTargetsFromOverride(items: MutableList<BatchedTrailerOverride>): MutableList<BatchedRelayTarget> {
    val out = mutableListOf<BatchedRelayTarget>()
    for (item in items) {
        val derived = newBatchedRelayTarget(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun splitBatchedRelayTarget(item: BatchedRelayTarget): Double = tieredTrailerWindowWeight(2, true)
