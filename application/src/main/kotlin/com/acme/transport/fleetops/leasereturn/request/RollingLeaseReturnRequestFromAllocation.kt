package com.acme.transport.fleetops.leasereturn.request

import com.acme.transport.fleetops.inspection.quota.InspectionAllocation
import com.acme.transport.fleetops.inspection.quota.inspectionAllocationBucket

fun summarizeInspectionAllocationsForRequest(items: MutableList<InspectionAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveRollingLeaseReturnRequestsFromAllocation(items: MutableList<InspectionAllocation>): MutableList<RollingLeaseReturnRequest> {
    val out = mutableListOf<RollingLeaseReturnRequest>()
    for (item in items) {
        val derived = newRollingLeaseReturnRequest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeRollingLeaseReturnRequest(item: RollingLeaseReturnRequest): String {
    return inspectionAllocationBucket(item.netLoad())
}
