package com.acme.transport.fleetops.utilization.segment

import com.acme.transport.fleetops.leasereturn.proposal.LeaseReturnDigest
import com.acme.transport.fleetops.leasereturn.proposal.normalizeLeaseReturnReservationCode

fun summarizeLeaseReturnDigestsForSchedule(items: MutableList<LeaseReturnDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveGlobalUtilizationSchedulesFromDigest(items: MutableList<LeaseReturnDigest>): MutableList<GlobalUtilizationSchedule> {
    val out = mutableListOf<GlobalUtilizationSchedule>()
    for (item in items) {
        val derived = newGlobalUtilizationSchedule(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun rankGlobalUtilizationSchedule(item: GlobalUtilizationSchedule): String {
    return normalizeLeaseReturnReservationCode(item.code)
}
