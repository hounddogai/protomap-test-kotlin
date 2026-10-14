package com.acme.transport.network.hub.hubestimate

import com.acme.transport.network.hub.hubplan.HubSegment
import com.acme.transport.network.hub.hubplan.hubSegmentBucket

fun summarizeHubSegmentsForEstimate(items: MutableList<HubSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveDefaultHubEstimatesFromSegment(items: MutableList<HubSegment>): MutableList<DefaultHubEstimate> {
    val out = mutableListOf<DefaultHubEstimate>()
    for (item in items) {
        val derived = newDefaultHubEstimate(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun mergeDefaultHubEstimate(item: DefaultHubEstimate): String = hubSegmentBucket(item.weightedTotal())
