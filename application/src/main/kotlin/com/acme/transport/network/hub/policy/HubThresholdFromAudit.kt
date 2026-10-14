package com.acme.transport.network.hub.policy

import com.acme.transport.network.servicearea.request.FallbackServiceAreaAudit
import com.acme.transport.network.servicearea.request.classifyServiceAreaEntry

fun summarizeFallbackServiceAreaAuditsForThreshold(items: MutableList<FallbackServiceAreaAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveHubThresholdsFromAudit(items: MutableList<FallbackServiceAreaAudit>): MutableList<HubThreshold> {
    val out = mutableListOf<HubThreshold>()
    for (item in items) {
        val derived = newHubThreshold(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun resolveHubThreshold(item: HubThreshold): String = classifyServiceAreaEntry(item.weightedLoad())
