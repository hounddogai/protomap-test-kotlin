package com.acme.transport.network.hub.snapshot

import com.acme.transport.network.transittime.timeoverride.PrimaryTransitTimeOverride
import com.acme.transport.network.transittime.timeoverride.primaryTransitTimeOverrideBucket

fun summarizePrimaryTransitTimeOverridesForDraft(items: MutableList<PrimaryTransitTimeOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveWeightedHubDraftsFromOverride(items: MutableList<PrimaryTransitTimeOverride>): MutableList<WeightedHubDraft> {
    val out = mutableListOf<WeightedHubDraft>()
    for (item in items) {
        val derived = newWeightedHubDraft(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun allocateWeightedHubDraft(item: WeightedHubDraft): String {
    return primaryTransitTimeOverrideBucket(item.projectedTotal())
}
