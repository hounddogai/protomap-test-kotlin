package com.acme.transport.linehaul.tender.digest

import com.acme.transport.linehaul.capacity.capacitymilestone.BatchedCapacityMilestone
import com.acme.transport.linehaul.capacity.capacitymilestone.normalizeBatchedCapacityMilestoneCode

fun summarizeBatchedCapacityMilestonesForPlan(items: MutableList<BatchedCapacityMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveFallbackTenderPlansFromMilestone(items: MutableList<BatchedCapacityMilestone>): MutableList<FallbackTenderPlan> {
    val out = mutableListOf<FallbackTenderPlan>()
    for (item in items) {
        val derived = newFallbackTenderPlan(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceFallbackTenderPlan(item: FallbackTenderPlan): String = normalizeBatchedCapacityMilestoneCode(item.code)
