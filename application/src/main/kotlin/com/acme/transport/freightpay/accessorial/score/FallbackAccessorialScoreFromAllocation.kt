package com.acme.transport.freightpay.accessorial.score

import com.acme.transport.freightpay.accessorial.allocation.WeightedAccessorialAllocation
import com.acme.transport.freightpay.accessorial.allocation.normalizeWeightedAccessorialAllocationCode

fun summarizeWeightedAccessorialAllocationsForScore(items: MutableList<WeightedAccessorialAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveFallbackAccessorialScoresFromAllocation(items: MutableList<WeightedAccessorialAllocation>): MutableList<FallbackAccessorialScore> {
    val out = mutableListOf<FallbackAccessorialScore>()
    for (item in items) {
        val derived = newFallbackAccessorialScore(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planFallbackAccessorialScore(item: FallbackAccessorialScore): String {
    return normalizeWeightedAccessorialAllocationCode(item.code)
}
