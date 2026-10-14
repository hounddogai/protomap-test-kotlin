package com.acme.transport.fleetops.leasereturn.returnschedule

import com.acme.transport.fleetops.leasereturn.budget.GlobalLeaseReturnAssignment
import com.acme.transport.fleetops.leasereturn.budget.classifyLeaseReturnBudget

fun summarizeGlobalLeaseReturnAssignmentsForRule(items: MutableList<GlobalLeaseReturnAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveBatchedLeaseReturnRulesFromAssignment(items: MutableList<GlobalLeaseReturnAssignment>): MutableList<BatchedLeaseReturnRule> {
    val out = mutableListOf<BatchedLeaseReturnRule>()
    for (item in items) {
        val derived = newBatchedLeaseReturnRule(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun applyBatchedLeaseReturnRule(item: BatchedLeaseReturnRule): String = classifyLeaseReturnBudget(item.netTotal())
