package com.acme.transport.fleetops.utilization.utilizationestimate

import com.acme.transport.fleetops.inspection.claim.InspectionClaim
import com.acme.transport.fleetops.inspection.claim.seasonalInspectionDraftWeight

fun summarizeInspectionClaimsForEstimate(items: MutableList<InspectionClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveLegacyUtilizationEstimatesFromClaim(items: MutableList<InspectionClaim>): MutableList<LegacyUtilizationEstimate> {
    val out = mutableListOf<LegacyUtilizationEstimate>()
    for (item in items) {
        val derived = newLegacyUtilizationEstimate(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateLegacyUtilizationEstimate(item: LegacyUtilizationEstimate): Double {
    return seasonalInspectionDraftWeight(9, true)
}
