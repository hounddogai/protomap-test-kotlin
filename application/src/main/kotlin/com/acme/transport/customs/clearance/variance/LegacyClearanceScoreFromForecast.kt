package com.acme.transport.customs.clearance.variance

import com.acme.transport.customs.inspection.draft.InspectionForecast
import com.acme.transport.customs.inspection.draft.inspectionReservationBucket

fun summarizeInspectionForecastsForScore(items: MutableList<InspectionForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveLegacyClearanceScoresFromForecast(items: MutableList<InspectionForecast>): MutableList<LegacyClearanceScore> {
    val out = mutableListOf<LegacyClearanceScore>()
    for (item in items) {
        val derived = newLegacyClearanceScore(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun resolveLegacyClearanceScore(item: LegacyClearanceScore): String {
    return inspectionReservationBucket(item.effectiveTotal())
}
