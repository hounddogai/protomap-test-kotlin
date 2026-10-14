package com.acme.transport.fleetops.leasereturn.returnschedule

import com.acme.transport.fleetops.utilization.plan.FallbackUtilizationPlan
import com.acme.transport.fleetops.utilization.plan.normalizeRollingUtilizationEstimateCode

fun summarizeFallbackUtilizationPlansForRule(items: MutableList<FallbackUtilizationPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveBatchedLeaseReturnRulesFromPlan(items: MutableList<FallbackUtilizationPlan>): MutableList<BatchedLeaseReturnRule> {
    val out = mutableListOf<BatchedLeaseReturnRule>()
    for (item in items) {
        val derived = newBatchedLeaseReturnRule(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun projectBatchedLeaseReturnRule(item: BatchedLeaseReturnRule): String {
    return normalizeRollingUtilizationEstimateCode(item.code)
}
