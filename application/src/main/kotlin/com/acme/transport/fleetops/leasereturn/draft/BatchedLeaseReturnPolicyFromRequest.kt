package com.acme.transport.fleetops.leasereturn.draft

import com.acme.transport.fleetops.leasereturn.request.RollingLeaseReturnRequest
import com.acme.transport.fleetops.leasereturn.request.classifyLeaseReturnAllocation

fun summarizeRollingLeaseReturnRequestsForPolicy(items: MutableList<RollingLeaseReturnRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveBatchedLeaseReturnPoliciesFromRequest(items: MutableList<RollingLeaseReturnRequest>): MutableList<BatchedLeaseReturnPolicy> {
    val out = mutableListOf<BatchedLeaseReturnPolicy>()
    for (item in items) {
        val derived = newBatchedLeaseReturnPolicy(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateBatchedLeaseReturnPolicy(item: BatchedLeaseReturnPolicy): String {
    return classifyLeaseReturnAllocation(item.netLoad())
}
