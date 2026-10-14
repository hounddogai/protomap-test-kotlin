package com.acme.transport.emissions.idling.manifest

import com.acme.transport.emissions.idling.window.AdaptiveIdlingDigest
import com.acme.transport.emissions.idling.window.adaptiveIdlingPolicyWeight

fun summarizeAdaptiveIdlingDigestsForManifest(items: MutableList<AdaptiveIdlingDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun derivePendingIdlingManifestsFromDigest(items: MutableList<AdaptiveIdlingDigest>): MutableList<PendingIdlingManifest> {
    val out = mutableListOf<PendingIdlingManifest>()
    for (item in items) {
        val derived = newPendingIdlingManifest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitPendingIdlingManifest(item: PendingIdlingManifest): Double = adaptiveIdlingPolicyWeight(6, true)
