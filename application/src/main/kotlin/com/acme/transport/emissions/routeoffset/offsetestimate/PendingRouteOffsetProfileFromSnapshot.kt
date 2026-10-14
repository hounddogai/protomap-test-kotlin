package com.acme.transport.emissions.routeoffset.offsetestimate

import com.acme.transport.emissions.idling.idlingsnapshot.AdaptiveIdlingSnapshot
import com.acme.transport.emissions.idling.idlingsnapshot.normalizePendingIdlingVarianceCode

fun summarizeAdaptiveIdlingSnapshotsForProfile(items: MutableList<AdaptiveIdlingSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun derivePendingRouteOffsetProfilesFromSnapshot(items: MutableList<AdaptiveIdlingSnapshot>): MutableList<PendingRouteOffsetProfile> {
    val out = mutableListOf<PendingRouteOffsetProfile>()
    for (item in items) {
        val derived = newPendingRouteOffsetProfile(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun planPendingRouteOffsetProfile(item: PendingRouteOffsetProfile): String {
    return normalizePendingIdlingVarianceCode(item.code)
}
