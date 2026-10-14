package com.acme.transport.network.transittime.draft

import com.acme.transport.network.zone.claim.CappedZoneClaim
import com.acme.transport.network.zone.claim.fallbackZoneThresholdBucket

fun summarizeCappedZoneClaimsForDraft(items: MutableList<CappedZoneClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveTransitTimeDraftsFromClaim(items: MutableList<CappedZoneClaim>): MutableList<TransitTimeDraft> {
    val out = mutableListOf<TransitTimeDraft>()
    for (item in items) {
        val derived = newTransitTimeDraft(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceTransitTimeDraft(item: TransitTimeDraft): String = fallbackZoneThresholdBucket(item.measuredValue())
