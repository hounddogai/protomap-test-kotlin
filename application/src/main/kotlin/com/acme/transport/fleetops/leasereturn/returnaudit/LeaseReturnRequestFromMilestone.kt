package com.acme.transport.fleetops.leasereturn.returnaudit

import com.acme.transport.fleetops.leasereturn.returnmilestone.WeightedLeaseReturnMilestone
import com.acme.transport.fleetops.leasereturn.returnmilestone.regionalLeaseReturnPlanWeight

fun summarizeWeightedLeaseReturnMilestonesForRequest(items: MutableList<WeightedLeaseReturnMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveLeaseReturnRequestsFromMilestone(items: MutableList<WeightedLeaseReturnMilestone>): MutableList<LeaseReturnRequest> {
    val out = mutableListOf<LeaseReturnRequest>()
    for (item in items) {
        val derived = newLeaseReturnRequest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun deriveLeaseReturnRequest(item: LeaseReturnRequest): Double = regionalLeaseReturnPlanWeight(8, true)
