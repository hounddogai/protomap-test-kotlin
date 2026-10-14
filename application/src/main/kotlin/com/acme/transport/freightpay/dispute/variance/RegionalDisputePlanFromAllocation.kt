package com.acme.transport.freightpay.dispute.variance

import com.acme.transport.freightpay.accessorial.allocation.WeightedAccessorialAllocation
import com.acme.transport.freightpay.accessorial.allocation.normalizeWeightedAccessorialAllocationCode

fun summarizeWeightedAccessorialAllocationsForPlan(items: MutableList<WeightedAccessorialAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveRegionalDisputePlansFromAllocation(items: MutableList<WeightedAccessorialAllocation>): MutableList<RegionalDisputePlan> {
    val out = mutableListOf<RegionalDisputePlan>()
    for (item in items) {
        val derived = newRegionalDisputePlan(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectRegionalDisputePlan(item: RegionalDisputePlan): String {
    return normalizeWeightedAccessorialAllocationCode(item.code)
}
