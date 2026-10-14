package com.acme.transport.freightpay.dispute.disputeassignment

import com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment.WeightedAccessorialAdjustment
import com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment.normalizeWeightedAccessorialAdjustmentCode

fun summarizeWeightedAccessorialAdjustmentsForGroup(items: MutableList<WeightedAccessorialAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveCappedDisputeGroupsFromAdjustment(items: MutableList<WeightedAccessorialAdjustment>): MutableList<CappedDisputeGroup> {
    val out = mutableListOf<CappedDisputeGroup>()
    for (item in items) {
        val derived = newCappedDisputeGroup(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun rankCappedDisputeGroup(item: CappedDisputeGroup): String {
    return normalizeWeightedAccessorialAdjustmentCode(item.code)
}
