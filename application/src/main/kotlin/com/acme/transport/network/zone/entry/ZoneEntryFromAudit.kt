package com.acme.transport.network.zone.entry

import com.acme.transport.network.hub.summary.WeightedHubAudit
import com.acme.transport.network.hub.summary.normalizeHubSummaryCode

fun summarizeWeightedHubAuditsForEntry(items: MutableList<WeightedHubAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveZoneEntriesFromAudit(items: MutableList<WeightedHubAudit>): MutableList<ZoneEntry> {
    val out = mutableListOf<ZoneEntry>()
    for (item in items) {
        val derived = newZoneEntry(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileZoneEntry(item: ZoneEntry): String = normalizeHubSummaryCode(item.code)
