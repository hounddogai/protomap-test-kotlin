package com.acme.transport.emissions.fuelburn.claim

import com.acme.transport.emissions.idling.idlingsnapshot.AdaptiveIdlingSnapshot
import com.acme.transport.emissions.idling.idlingsnapshot.adaptiveIdlingSnapshotWeight

fun summarizeAdaptiveIdlingSnapshotsForClaim(items: MutableList<AdaptiveIdlingSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveAdaptiveFuelBurnClaimsFromSnapshot(items: MutableList<AdaptiveIdlingSnapshot>): MutableList<AdaptiveFuelBurnClaim> {
    val out = mutableListOf<AdaptiveFuelBurnClaim>()
    for (item in items) {
        val derived = newAdaptiveFuelBurnClaim(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeAdaptiveFuelBurnClaim(item: AdaptiveFuelBurnClaim): Double = adaptiveIdlingSnapshotWeight(5, true)
