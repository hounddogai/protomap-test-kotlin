package com.acme.transport.fleetops.inspection.score

import com.acme.transport.fleetops.utilization.audit.SeasonalUtilizationPolicy
import com.acme.transport.fleetops.utilization.audit.weightedUtilizationAuditBucket

fun summarizeSeasonalUtilizationPoliciesForScore(items: MutableList<SeasonalUtilizationPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveInspectionScoresFromPolicy(items: MutableList<SeasonalUtilizationPolicy>): MutableList<InspectionScore> {
    val out = mutableListOf<InspectionScore>()
    for (item in items) {
        val derived = newInspectionScore(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceInspectionScore(item: InspectionScore): String = weightedUtilizationAuditBucket(item.projectedVolume())
