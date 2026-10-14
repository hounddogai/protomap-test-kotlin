package com.acme.transport.network.cutoff.slot

import com.acme.transport.network.hub.primaryhubsummary.GlobalHubSummary
import com.acme.transport.network.hub.primaryhubsummary.classifyGlobalHubSummary

fun summarizeGlobalHubSummariesForDigest(items: MutableList<GlobalHubSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveCutoffDigestsFromSummary(items: MutableList<GlobalHubSummary>): MutableList<CutoffDigest> {
    val out = mutableListOf<CutoffDigest>()
    for (item in items) {
        val derived = newCutoffDigest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeCutoffDigest(item: CutoffDigest): String = classifyGlobalHubSummary(item.measuredValue())
