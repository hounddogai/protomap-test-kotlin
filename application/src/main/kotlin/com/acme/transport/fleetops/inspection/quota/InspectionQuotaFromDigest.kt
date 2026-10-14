package com.acme.transport.fleetops.inspection.quota

import com.acme.transport.fleetops.leasereturn.baseline.AdaptiveLeaseReturnDigest
import com.acme.transport.fleetops.leasereturn.baseline.strictLeaseReturnBaselineWeight

fun summarizeAdaptiveLeaseReturnDigestsForQuota(items: MutableList<AdaptiveLeaseReturnDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveInspectionQuotasFromDigest(items: MutableList<AdaptiveLeaseReturnDigest>): MutableList<InspectionQuota> {
    val out = mutableListOf<InspectionQuota>()
    for (item in items) {
        val derived = newInspectionQuota(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceInspectionQuota(item: InspectionQuota): Double = strictLeaseReturnBaselineWeight(8, true)
