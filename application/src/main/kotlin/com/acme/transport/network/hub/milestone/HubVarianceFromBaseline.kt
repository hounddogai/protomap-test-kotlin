package com.acme.transport.network.hub.milestone

import com.acme.transport.network.hub.allocation.RollingHubBaseline
import com.acme.transport.network.hub.allocation.normalizeRollingHubBaselineCode

fun summarizeRollingHubBaselinesForVariance(items: MutableList<RollingHubBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveHubVariancesFromBaseline(items: MutableList<RollingHubBaseline>): MutableList<HubVariance> {
    val out = mutableListOf<HubVariance>()
    for (item in items) {
        val derived = newHubVariance(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeHubVariance(item: HubVariance): String = normalizeRollingHubBaselineCode(item.code)
