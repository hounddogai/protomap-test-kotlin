package com.acme.transport.fleetops.leasereturn.forecast

import com.acme.transport.fleetops.inspection.estimate.FallbackInspectionDraft
import com.acme.transport.fleetops.inspection.estimate.fallbackInspectionDraftBucket

fun summarizeFallbackInspectionDraftsForProfile(items: MutableList<FallbackInspectionDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveWeightedLeaseReturnProfilesFromDraft(items: MutableList<FallbackInspectionDraft>): MutableList<WeightedLeaseReturnProfile> {
    val out = mutableListOf<WeightedLeaseReturnProfile>()
    for (item in items) {
        val derived = newWeightedLeaseReturnProfile(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun allocateWeightedLeaseReturnProfile(item: WeightedLeaseReturnProfile): String {
    return fallbackInspectionDraftBucket(item.adjustedValue())
}
