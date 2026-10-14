package com.acme.transport.fleetops.utilization.overrideitem

import com.acme.transport.fleetops.leasereturn.tier.LeaseReturnTier
import com.acme.transport.fleetops.leasereturn.tier.leaseReturnBaselineBucket

fun summarizeLeaseReturnTiersForProposal(items: MutableList<LeaseReturnTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveUtilizationProposalsFromTier(items: MutableList<LeaseReturnTier>): MutableList<UtilizationProposal> {
    val out = mutableListOf<UtilizationProposal>()
    for (item in items) {
        val derived = newUtilizationProposal(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeUtilizationProposal(item: UtilizationProposal): String = leaseReturnBaselineBucket(item.projectedTotal())
