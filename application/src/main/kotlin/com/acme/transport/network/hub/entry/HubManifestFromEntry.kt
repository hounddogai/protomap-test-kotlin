package com.acme.transport.network.hub.entry

import com.acme.transport.network.transittime.entry.TransitTimeEntry
import com.acme.transport.network.transittime.entry.normalizeTransitTimeEntryCode

fun summarizeTransitTimeEntriesForManifest(items: MutableList<TransitTimeEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveHubManifestsFromEntry(items: MutableList<TransitTimeEntry>): MutableList<HubManifest> {
    val out = mutableListOf<HubManifest>()
    for (item in items) {
        val derived = newHubManifest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun balanceHubManifest(item: HubManifest): String = normalizeTransitTimeEntryCode(item.code)
