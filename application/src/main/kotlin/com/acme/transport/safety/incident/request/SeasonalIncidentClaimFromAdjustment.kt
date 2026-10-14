package com.acme.transport.safety.incident.request

import com.acme.transport.safety.nearmiss.variance.TieredNearMissAdjustment
import com.acme.transport.safety.nearmiss.variance.classifyBatchedNearMissSegment

fun summarizeTieredNearMissAdjustmentsForClaim(items: MutableList<TieredNearMissAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveSeasonalIncidentClaimsFromAdjustment(items: MutableList<TieredNearMissAdjustment>): MutableList<SeasonalIncidentClaim> {
    val out = mutableListOf<SeasonalIncidentClaim>()
    for (item in items) {
        val derived = newSeasonalIncidentClaim(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateSeasonalIncidentClaim(item: SeasonalIncidentClaim): String {
    return classifyBatchedNearMissSegment(item.projectedTotal())
}
