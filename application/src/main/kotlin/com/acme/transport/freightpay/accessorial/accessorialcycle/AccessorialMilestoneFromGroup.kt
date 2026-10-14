package com.acme.transport.freightpay.accessorial.accessorialcycle

import com.acme.transport.freightpay.dispute.group.StrictDisputeGroup
import com.acme.transport.freightpay.dispute.group.globalDisputeReservationWeight

fun summarizeStrictDisputeGroupsForMilestone(items: MutableList<StrictDisputeGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveAccessorialMilestonesFromGroup(items: MutableList<StrictDisputeGroup>): MutableList<AccessorialMilestone> {
    val out = mutableListOf<AccessorialMilestone>()
    for (item in items) {
        val derived = newAccessorialMilestone(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun resolveAccessorialMilestone(item: AccessorialMilestone): Double = globalDisputeReservationWeight(7, true)
