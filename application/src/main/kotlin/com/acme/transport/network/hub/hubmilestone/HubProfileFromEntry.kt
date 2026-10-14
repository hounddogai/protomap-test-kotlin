package com.acme.transport.network.hub.hubmilestone

import com.acme.transport.network.hub.forecast.PendingHubEntry
import com.acme.transport.network.hub.forecast.pendingHubEntryBucket

fun summarizePendingHubEntriesForProfile(items: MutableList<PendingHubEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveHubProfilesFromEntry(items: MutableList<PendingHubEntry>): MutableList<HubProfile> {
    val out = mutableListOf<HubProfile>()
    for (item in items) {
        val derived = newHubProfile(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeHubProfile(item: HubProfile): String = pendingHubEntryBucket(item.projectedVolume())
