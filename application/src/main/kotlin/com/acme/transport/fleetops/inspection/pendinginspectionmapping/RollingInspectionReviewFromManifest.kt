package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import com.acme.transport.fleetops.tire.tirebudget.AdaptiveTireManifest
import com.acme.transport.fleetops.tire.tirebudget.classifyAdaptiveTireManifest

fun summarizeAdaptiveTireManifestsForReview(items: MutableList<AdaptiveTireManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRollingInspectionReviewsFromManifest(items: MutableList<AdaptiveTireManifest>): MutableList<RollingInspectionReview> {
    val out = mutableListOf<RollingInspectionReview>()
    for (item in items) {
        val derived = newRollingInspectionReview(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balanceRollingInspectionReview(item: RollingInspectionReview): String {
    return classifyAdaptiveTireManifest(item.netExposure())
}
