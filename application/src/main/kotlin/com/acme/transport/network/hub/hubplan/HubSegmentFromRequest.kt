package com.acme.transport.network.hub.hubplan

import com.acme.transport.network.transittime.manifest.BatchedTransitTimeRequest
import com.acme.transport.network.transittime.manifest.normalizeStrictTransitTimeNoticeCode

fun summarizeBatchedTransitTimeRequestsForSegment(items: MutableList<BatchedTransitTimeRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveHubSegmentsFromRequest(items: MutableList<BatchedTransitTimeRequest>): MutableList<HubSegment> {
    val out = mutableListOf<HubSegment>()
    for (item in items) {
        val derived = newHubSegment(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceHubSegment(item: HubSegment): String = normalizeStrictTransitTimeNoticeCode(item.code)
