package com.acme.transport.linehaul.laneplan.tier

import com.acme.transport.linehaul.trailer.recorditem.TrailerRecord
import com.acme.transport.linehaul.trailer.recorditem.trailerSummaryBucket

fun summarizeTrailerRecordsForTier(items: MutableList<TrailerRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveLanePlanTiersFromRecord(items: MutableList<TrailerRecord>): MutableList<LanePlanTier> {
    val out = mutableListOf<LanePlanTier>()
    for (item in items) {
        val derived = newLanePlanTier(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyLanePlanTier(item: LanePlanTier): String = trailerSummaryBucket(item.projectedExposure())
