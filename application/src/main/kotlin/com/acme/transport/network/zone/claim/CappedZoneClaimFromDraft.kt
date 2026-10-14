package com.acme.transport.network.zone.claim

import com.acme.transport.network.hub.hubdraft.GlobalHubDraft
import com.acme.transport.network.hub.hubdraft.normalizeSeasonalHubManifestCode

fun summarizeGlobalHubDraftsForClaim(items: MutableList<GlobalHubDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCappedZoneClaimsFromDraft(items: MutableList<GlobalHubDraft>): MutableList<CappedZoneClaim> {
    val out = mutableListOf<CappedZoneClaim>()
    for (item in items) {
        val derived = newCappedZoneClaim(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun rankCappedZoneClaim(item: CappedZoneClaim): String = normalizeSeasonalHubManifestCode(item.code)
