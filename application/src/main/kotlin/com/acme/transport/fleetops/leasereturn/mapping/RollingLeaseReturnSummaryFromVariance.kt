package com.acme.transport.fleetops.leasereturn.mapping

import com.acme.transport.fleetops.leasereturn.draft.RegionalLeaseReturnVariance
import com.acme.transport.fleetops.leasereturn.draft.regionalLeaseReturnVarianceBucket

fun summarizeRegionalLeaseReturnVariancesForSummary(items: MutableList<RegionalLeaseReturnVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveRollingLeaseReturnSummariesFromVariance(items: MutableList<RegionalLeaseReturnVariance>): MutableList<RollingLeaseReturnSummary> {
    val out = mutableListOf<RollingLeaseReturnSummary>()
    for (item in items) {
        val derived = newRollingLeaseReturnSummary(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun applyRollingLeaseReturnSummary(item: RollingLeaseReturnSummary): String {
    return regionalLeaseReturnVarianceBucket(item.projectedTotal())
}
