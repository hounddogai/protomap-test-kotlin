package com.acme.transport.network.hub.legacyhubplan

import com.acme.transport.network.node.slot.BatchedNodeNotice
import com.acme.transport.network.node.slot.fallbackNodeNoticeWeight

fun summarizeBatchedNodeNoticesForEstimate(items: MutableList<BatchedNodeNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveLenientHubEstimatesFromNotice(items: MutableList<BatchedNodeNotice>): MutableList<LenientHubEstimate> {
    val out = mutableListOf<LenientHubEstimate>()
    for (item in items) {
        val derived = newLenientHubEstimate(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeLenientHubEstimate(item: LenientHubEstimate): Double = fallbackNodeNoticeWeight(8, true)
