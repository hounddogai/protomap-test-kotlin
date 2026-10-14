package com.acme.transport.fleetops.utilization.utilizationsummary

import com.acme.transport.fleetops.leasereturn.draft.RegionalLeaseReturnVariance
import com.acme.transport.fleetops.leasereturn.draft.globalLeaseReturnProposalBucket

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

fun deriveAdaptiveUtilizationSummariesFromVariance(items: MutableList<RegionalLeaseReturnVariance>): MutableList<AdaptiveUtilizationSummary> {
    val out = mutableListOf<AdaptiveUtilizationSummary>()
    for (item in items) {
        val derived = newAdaptiveUtilizationSummary(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectAdaptiveUtilizationSummary(item: AdaptiveUtilizationSummary): String {
    return globalLeaseReturnProposalBucket(item.netVolume())
}
