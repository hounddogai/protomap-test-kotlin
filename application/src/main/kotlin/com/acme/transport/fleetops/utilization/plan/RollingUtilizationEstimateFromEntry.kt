package com.acme.transport.fleetops.utilization.plan

import com.acme.transport.fleetops.leasereturn.budget.LeaseReturnEntry
import com.acme.transport.fleetops.leasereturn.budget.normalizeLeaseReturnBudgetCode

fun summarizeLeaseReturnEntriesForEstimate(items: MutableList<LeaseReturnEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveRollingUtilizationEstimatesFromEntry(items: MutableList<LeaseReturnEntry>): MutableList<RollingUtilizationEstimate> {
    val out = mutableListOf<RollingUtilizationEstimate>()
    for (item in items) {
        val derived = newRollingUtilizationEstimate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scoreRollingUtilizationEstimate(item: RollingUtilizationEstimate): String {
    return normalizeLeaseReturnBudgetCode(item.code)
}
