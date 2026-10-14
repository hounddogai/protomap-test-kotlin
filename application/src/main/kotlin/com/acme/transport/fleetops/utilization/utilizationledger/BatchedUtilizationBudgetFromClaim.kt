package com.acme.transport.fleetops.utilization.utilizationledger

import com.acme.transport.fleetops.utilization.group.ManualUtilizationClaim
import com.acme.transport.fleetops.utilization.group.normalizeTieredUtilizationGroupCode

fun summarizeManualUtilizationClaimsForBudget(items: MutableList<ManualUtilizationClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveBatchedUtilizationBudgetsFromClaim(items: MutableList<ManualUtilizationClaim>): MutableList<BatchedUtilizationBudget> {
    val out = mutableListOf<BatchedUtilizationBudget>()
    for (item in items) {
        val derived = newBatchedUtilizationBudget(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceBatchedUtilizationBudget(item: BatchedUtilizationBudget): String {
    return normalizeTieredUtilizationGroupCode(item.code)
}
