package com.acme.transport.fleetops.inspection.claim

import com.acme.transport.fleetops.utilization.plan.FallbackUtilizationPlan
import com.acme.transport.fleetops.utilization.plan.normalizeFallbackUtilizationPlanCode

fun summarizeFallbackUtilizationPlansForClaim(items: MutableList<FallbackUtilizationPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveInspectionClaimsFromPlan(items: MutableList<FallbackUtilizationPlan>): MutableList<InspectionClaim> {
    val out = mutableListOf<InspectionClaim>()
    for (item in items) {
        val derived = newInspectionClaim(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun rankInspectionClaim(item: InspectionClaim): String = normalizeFallbackUtilizationPlanCode(item.code)
