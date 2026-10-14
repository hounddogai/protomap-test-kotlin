package com.acme.transport.customs.clearance.tier

import com.acme.transport.customs.inspection.inspectiondigest.SeasonalInspectionDigest
import com.acme.transport.customs.inspection.inspectiondigest.normalizeInspectionNoticeCode

fun summarizeSeasonalInspectionDigestsForTier(items: MutableList<SeasonalInspectionDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveClearanceTiersFromDigest(items: MutableList<SeasonalInspectionDigest>): MutableList<ClearanceTier> {
    val out = mutableListOf<ClearanceTier>()
    for (item in items) {
        val derived = newClearanceTier(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankClearanceTier(item: ClearanceTier): String = normalizeInspectionNoticeCode(item.code)
