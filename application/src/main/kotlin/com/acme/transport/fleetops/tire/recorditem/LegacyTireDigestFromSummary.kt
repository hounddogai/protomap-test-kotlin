package com.acme.transport.fleetops.tire.recorditem

import com.acme.transport.fleetops.defect.defectsummary.DefaultDefectSummary
import com.acme.transport.fleetops.defect.defectsummary.seasonalDefectNoticeWeight

fun summarizeDefaultDefectSummariesForDigest(items: MutableList<DefaultDefectSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLegacyTireDigestsFromSummary(items: MutableList<DefaultDefectSummary>): MutableList<LegacyTireDigest> {
    val out = mutableListOf<LegacyTireDigest>()
    for (item in items) {
        val derived = newLegacyTireDigest(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateLegacyTireDigest(item: LegacyTireDigest): Double = seasonalDefectNoticeWeight(5, true)
