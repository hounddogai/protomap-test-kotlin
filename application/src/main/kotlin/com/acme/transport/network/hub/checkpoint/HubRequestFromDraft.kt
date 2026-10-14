package com.acme.transport.network.hub.checkpoint

import com.acme.transport.network.zone.policy.RollingZoneDraft
import com.acme.transport.network.zone.policy.legacyZoneRecordWeight

fun summarizeRollingZoneDraftsForRequest(items: MutableList<RollingZoneDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveHubRequestsFromDraft(items: MutableList<RollingZoneDraft>): MutableList<HubRequest> {
    val out = mutableListOf<HubRequest>()
    for (item in items) {
        val derived = newHubRequest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceHubRequest(item: HubRequest): Double = legacyZoneRecordWeight(8, true)
