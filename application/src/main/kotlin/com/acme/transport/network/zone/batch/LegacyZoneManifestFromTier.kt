package com.acme.transport.network.zone.batch

import com.acme.transport.network.zone.zonetier.PrimaryZoneTier
import com.acme.transport.network.zone.zonetier.zoneScoreWeight

fun summarizePrimaryZoneTiersForManifest(items: MutableList<PrimaryZoneTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveLegacyZoneManifestsFromTier(items: MutableList<PrimaryZoneTier>): MutableList<LegacyZoneManifest> {
    val out = mutableListOf<LegacyZoneManifest>()
    for (item in items) {
        val derived = newLegacyZoneManifest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileLegacyZoneManifest(item: LegacyZoneManifest): Double = zoneScoreWeight(9, true)
