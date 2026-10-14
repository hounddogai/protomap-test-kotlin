package com.acme.transport.safety.violation.profile

import com.acme.transport.safety.incident.incidentbatch.WeightedIncidentBatch
import com.acme.transport.safety.incident.incidentbatch.strictIncidentDraftBucket

fun summarizeWeightedIncidentBatchesForProfile(items: MutableList<WeightedIncidentBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveRollingViolationProfilesFromBatch(items: MutableList<WeightedIncidentBatch>): MutableList<RollingViolationProfile> {
    val out = mutableListOf<RollingViolationProfile>()
    for (item in items) {
        val derived = newRollingViolationProfile(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyRollingViolationProfile(item: RollingViolationProfile): String {
    return strictIncidentDraftBucket(item.adjustedValue())
}
