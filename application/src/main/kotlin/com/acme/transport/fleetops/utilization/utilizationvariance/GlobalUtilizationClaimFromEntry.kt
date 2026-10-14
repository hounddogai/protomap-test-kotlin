package com.acme.transport.fleetops.utilization.utilizationvariance

import com.acme.transport.fleetops.leasereturn.mapping.ManualLeaseReturnEntry
import com.acme.transport.fleetops.leasereturn.mapping.normalizeScheduledLeaseReturnMappingCode

fun summarizeManualLeaseReturnEntriesForClaim(items: MutableList<ManualLeaseReturnEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveGlobalUtilizationClaimsFromEntry(items: MutableList<ManualLeaseReturnEntry>): MutableList<GlobalUtilizationClaim> {
    val out = mutableListOf<GlobalUtilizationClaim>()
    for (item in items) {
        val derived = newGlobalUtilizationClaim(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignGlobalUtilizationClaim(item: GlobalUtilizationClaim): String {
    return normalizeScheduledLeaseReturnMappingCode(item.code)
}
