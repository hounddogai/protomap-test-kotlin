package com.acme.transport.network.hub.summary

import com.acme.transport.network.hub.hubentry.HubBundle

fun summarizeHubBundlesForAudit(items: MutableList<HubBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveWeightedHubAuditsFromBundle(items: MutableList<HubBundle>): MutableList<WeightedHubAudit> {
    val out = mutableListOf<WeightedHubAudit>()
    for (item in items) {
        val derived = newWeightedHubAudit(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}
