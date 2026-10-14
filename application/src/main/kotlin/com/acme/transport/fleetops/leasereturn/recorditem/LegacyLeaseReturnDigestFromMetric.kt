package com.acme.transport.fleetops.leasereturn.recorditem

import com.acme.transport.fleetops.utilization.score.UtilizationMetric
import com.acme.transport.fleetops.utilization.score.normalizeUtilizationTierCode

fun summarizeUtilizationMetricsForDigest(items: MutableList<UtilizationMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveLegacyLeaseReturnDigestsFromMetric(items: MutableList<UtilizationMetric>): MutableList<LegacyLeaseReturnDigest> {
    val out = mutableListOf<LegacyLeaseReturnDigest>()
    for (item in items) {
        val derived = newLegacyLeaseReturnDigest(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeLegacyLeaseReturnDigest(item: LegacyLeaseReturnDigest): String {
    return normalizeUtilizationTierCode(item.code)
}
