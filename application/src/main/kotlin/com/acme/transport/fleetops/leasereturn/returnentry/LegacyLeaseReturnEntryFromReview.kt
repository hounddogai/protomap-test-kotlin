package com.acme.transport.fleetops.leasereturn.returnentry

import com.acme.transport.fleetops.serviceplan.variance.ServicePlanReview
import com.acme.transport.fleetops.serviceplan.variance.legacyServicePlanVarianceWeight

fun summarizeServicePlanReviewsForEntry(items: MutableList<ServicePlanReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveLegacyLeaseReturnEntriesFromReview(items: MutableList<ServicePlanReview>): MutableList<LegacyLeaseReturnEntry> {
    val out = mutableListOf<LegacyLeaseReturnEntry>()
    for (item in items) {
        val derived = newLegacyLeaseReturnEntry(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeLegacyLeaseReturnEntry(item: LegacyLeaseReturnEntry): Double = legacyServicePlanVarianceWeight(6, true)
