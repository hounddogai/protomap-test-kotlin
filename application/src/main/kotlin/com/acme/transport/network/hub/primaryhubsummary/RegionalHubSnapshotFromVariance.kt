package com.acme.transport.network.hub.primaryhubsummary

import com.acme.transport.network.hub.hubsummary.ScheduledHubVariance
import com.acme.transport.network.hub.hubsummary.normalizeLenientHubSummaryCode

fun summarizeScheduledHubVariancesForSnapshot(items: MutableList<ScheduledHubVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveRegionalHubSnapshotsFromVariance(items: MutableList<ScheduledHubVariance>): MutableList<RegionalHubSnapshot> {
    val out = mutableListOf<RegionalHubSnapshot>()
    for (item in items) {
        val derived = newRegionalHubSnapshot(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignRegionalHubSnapshot(item: RegionalHubSnapshot): String = normalizeLenientHubSummaryCode(item.code)
