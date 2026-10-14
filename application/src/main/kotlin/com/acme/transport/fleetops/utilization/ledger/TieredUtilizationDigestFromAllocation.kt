package com.acme.transport.fleetops.utilization.ledger

import com.acme.transport.fleetops.leasereturn.request.LeaseReturnAllocation
import com.acme.transport.fleetops.leasereturn.request.normalizeRollingLeaseReturnRequestCode

fun summarizeLeaseReturnAllocationsForDigest(items: MutableList<LeaseReturnAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveTieredUtilizationDigestsFromAllocation(items: MutableList<LeaseReturnAllocation>): MutableList<TieredUtilizationDigest> {
    val out = mutableListOf<TieredUtilizationDigest>()
    for (item in items) {
        val derived = newTieredUtilizationDigest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun matchTieredUtilizationDigest(item: TieredUtilizationDigest): String {
    return normalizeRollingLeaseReturnRequestCode(item.code)
}
