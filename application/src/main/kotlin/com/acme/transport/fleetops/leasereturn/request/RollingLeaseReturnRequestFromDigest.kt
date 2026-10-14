package com.acme.transport.fleetops.leasereturn.request

import com.acme.transport.fleetops.leasereturn.baseline.AdaptiveLeaseReturnDigest
import com.acme.transport.fleetops.leasereturn.baseline.strictLeaseReturnBaselineBucket

fun summarizeAdaptiveLeaseReturnDigestsForRequest(items: MutableList<AdaptiveLeaseReturnDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveRollingLeaseReturnRequestsFromDigest(items: MutableList<AdaptiveLeaseReturnDigest>): MutableList<RollingLeaseReturnRequest> {
    val out = mutableListOf<RollingLeaseReturnRequest>()
    for (item in items) {
        val derived = newRollingLeaseReturnRequest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchRollingLeaseReturnRequest(item: RollingLeaseReturnRequest): String {
    return strictLeaseReturnBaselineBucket(item.netLoad())
}
