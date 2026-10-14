package com.acme.transport.lastmile.parcel.policy

import com.acme.transport.lastmile.redelivery.claim.RegionalRedeliveryClaim
import com.acme.transport.lastmile.redelivery.claim.normalizePrimaryRedeliveryPreferenceCode

fun summarizeRegionalRedeliveryClaimsForSnapshot(items: MutableList<RegionalRedeliveryClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveParcelSnapshotsFromClaim(items: MutableList<RegionalRedeliveryClaim>): MutableList<ParcelSnapshot> {
    val out = mutableListOf<ParcelSnapshot>()
    for (item in items) {
        val derived = newParcelSnapshot(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchParcelSnapshot(item: ParcelSnapshot): String = normalizePrimaryRedeliveryPreferenceCode(item.code)
