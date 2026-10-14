package com.acme.transport.network.transittime.summary

import com.acme.transport.network.hub.tieredhubplan.DefaultHubMapping
import com.acme.transport.network.hub.tieredhubplan.scheduledHubCheckpointBucket

fun summarizeDefaultHubMappingsForSummary(items: MutableList<DefaultHubMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveScheduledTransitTimeSummariesFromMapping(items: MutableList<DefaultHubMapping>): MutableList<ScheduledTransitTimeSummary> {
    val out = mutableListOf<ScheduledTransitTimeSummary>()
    for (item in items) {
        val derived = newScheduledTransitTimeSummary(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun settleScheduledTransitTimeSummary(item: ScheduledTransitTimeSummary): String {
    return scheduledHubCheckpointBucket(item.effectiveLoad())
}
