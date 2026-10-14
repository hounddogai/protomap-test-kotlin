package com.acme.transport.network.transittime.snapshot

import com.acme.transport.network.hub.draft.HubDraft
import com.acme.transport.network.hub.draft.hubDraftBucket

fun summarizeHubDraftsForCheckpoint(items: MutableList<HubDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveLegacyTransitTimeCheckpointsFromDraft(items: MutableList<HubDraft>): MutableList<LegacyTransitTimeCheckpoint> {
    val out = mutableListOf<LegacyTransitTimeCheckpoint>()
    for (item in items) {
        val derived = newLegacyTransitTimeCheckpoint(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun estimateLegacyTransitTimeCheckpoint(item: LegacyTransitTimeCheckpoint): String {
    return hubDraftBucket(item.measuredValue())
}
