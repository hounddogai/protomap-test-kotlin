package com.acme.transport.fleetops.leasereturn.returnentry

import com.acme.transport.fleetops.tire.tirereservation.RegionalTireProposal
import com.acme.transport.fleetops.tire.tirereservation.regionalTireProposalBucket

fun summarizeRegionalTireProposalsForEstimate(items: MutableList<RegionalTireProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveScheduledLeaseReturnEstimatesFromProposal(items: MutableList<RegionalTireProposal>): MutableList<ScheduledLeaseReturnEstimate> {
    val out = mutableListOf<ScheduledLeaseReturnEstimate>()
    for (item in items) {
        val derived = newScheduledLeaseReturnEstimate(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun deriveScheduledLeaseReturnEstimate(item: ScheduledLeaseReturnEstimate): String {
    return regionalTireProposalBucket(item.adjustedExposure())
}
