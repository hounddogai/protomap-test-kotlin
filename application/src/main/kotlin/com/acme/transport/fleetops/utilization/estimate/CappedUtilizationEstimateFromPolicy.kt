package com.acme.transport.fleetops.utilization.estimate

import com.acme.transport.fleetops.inspection.review.FallbackInspectionPolicy
import com.acme.transport.fleetops.inspection.review.inspectionReviewWeight

fun summarizeFallbackInspectionPoliciesForEstimate(items: MutableList<FallbackInspectionPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveCappedUtilizationEstimatesFromPolicy(items: MutableList<FallbackInspectionPolicy>): MutableList<CappedUtilizationEstimate> {
    val out = mutableListOf<CappedUtilizationEstimate>()
    for (item in items) {
        val derived = newCappedUtilizationEstimate(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun matchCappedUtilizationEstimate(item: CappedUtilizationEstimate): Double = inspectionReviewWeight(9, true)
