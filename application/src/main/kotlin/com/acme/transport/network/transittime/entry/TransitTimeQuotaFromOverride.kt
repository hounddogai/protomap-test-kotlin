package com.acme.transport.network.transittime.entry

import com.acme.transport.network.hub.overrideitem.HubOverride
import com.acme.transport.network.hub.overrideitem.normalizeHubProposalCode

fun summarizeHubOverridesForQuota(items: MutableList<HubOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveTransitTimeQuotasFromOverride(items: MutableList<HubOverride>): MutableList<TransitTimeQuota> {
    val out = mutableListOf<TransitTimeQuota>()
    for (item in items) {
        val derived = newTransitTimeQuota(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun rankTransitTimeQuota(item: TransitTimeQuota): String = normalizeHubProposalCode(item.code)
