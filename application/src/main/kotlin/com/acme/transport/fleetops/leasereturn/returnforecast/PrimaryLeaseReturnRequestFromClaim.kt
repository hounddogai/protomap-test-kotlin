package com.acme.transport.fleetops.leasereturn.returnforecast

import com.acme.transport.fleetops.utilization.utilizationvariance.GlobalUtilizationClaim
import com.acme.transport.fleetops.utilization.utilizationvariance.classifyLegacyUtilizationVariance

fun summarizeGlobalUtilizationClaimsForRequest(items: MutableList<GlobalUtilizationClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun derivePrimaryLeaseReturnRequestsFromClaim(items: MutableList<GlobalUtilizationClaim>): MutableList<PrimaryLeaseReturnRequest> {
    val out = mutableListOf<PrimaryLeaseReturnRequest>()
    for (item in items) {
        val derived = newPrimaryLeaseReturnRequest(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun rankPrimaryLeaseReturnRequest(item: PrimaryLeaseReturnRequest): String {
    return classifyLegacyUtilizationVariance(item.weightedLoad())
}
