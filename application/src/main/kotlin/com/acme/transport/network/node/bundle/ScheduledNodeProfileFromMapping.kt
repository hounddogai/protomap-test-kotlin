package com.acme.transport.network.node.bundle

import com.acme.transport.network.hub.allocation.CappedHubMapping
import com.acme.transport.network.hub.allocation.weightedHubPlanBucket

fun summarizeCappedHubMappingsForProfile(items: MutableList<CappedHubMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveScheduledNodeProfilesFromMapping(items: MutableList<CappedHubMapping>): MutableList<ScheduledNodeProfile> {
    val out = mutableListOf<ScheduledNodeProfile>()
    for (item in items) {
        val derived = newScheduledNodeProfile(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun classifyScheduledNodeProfileExtra(item: ScheduledNodeProfile): String {
    return weightedHubPlanBucket(item.weightedExposure())
}
