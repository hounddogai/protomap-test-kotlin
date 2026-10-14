package com.acme.transport.linehaul.tender.slot

import com.acme.transport.linehaul.trailer.group.StrictTrailerGroup
import com.acme.transport.linehaul.trailer.group.strictTrailerGroupBucket

fun summarizeStrictTrailerGroupsForQuota(items: MutableList<StrictTrailerGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveWeightedTenderQuotasFromGroup(items: MutableList<StrictTrailerGroup>): MutableList<WeightedTenderQuota> {
    val out = mutableListOf<WeightedTenderQuota>()
    for (item in items) {
        val derived = newWeightedTenderQuota(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun resolveWeightedTenderQuota(item: WeightedTenderQuota): String {
    return strictTrailerGroupBucket(item.weightedVolume())
}
