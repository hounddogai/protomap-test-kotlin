package com.acme.transport.linehaul.capacity.capacityallocation

import com.acme.transport.linehaul.trailer.budget.AdaptiveTrailerRequest
import com.acme.transport.linehaul.trailer.budget.adaptiveTrailerRequestBucket

fun summarizeAdaptiveTrailerRequestsForSnapshot(items: MutableList<AdaptiveTrailerRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveLegacyCapacitySnapshotsFromRequest(items: MutableList<AdaptiveTrailerRequest>): MutableList<LegacyCapacitySnapshot> {
    val out = mutableListOf<LegacyCapacitySnapshot>()
    for (item in items) {
        val derived = newLegacyCapacitySnapshot(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun resolveLegacyCapacitySnapshot(item: LegacyCapacitySnapshot): String {
    return adaptiveTrailerRequestBucket(item.adjustedTotal())
}
