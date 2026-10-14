package com.acme.transport.network.hub.hubdraft

import com.acme.transport.network.zone.baseline.WeightedZoneManifest
import com.acme.transport.network.zone.baseline.weightedZoneManifestWeight

fun summarizeWeightedZoneManifestsForManifest(items: MutableList<WeightedZoneManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveSeasonalHubManifestsFromManifest(items: MutableList<WeightedZoneManifest>): MutableList<SeasonalHubManifest> {
    val out = mutableListOf<SeasonalHubManifest>()
    for (item in items) {
        val derived = newSeasonalHubManifest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveSeasonalHubManifest(item: SeasonalHubManifest): Double = weightedZoneManifestWeight(6, true)
