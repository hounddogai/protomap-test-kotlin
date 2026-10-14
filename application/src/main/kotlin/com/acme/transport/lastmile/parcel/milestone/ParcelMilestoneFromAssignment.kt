package com.acme.transport.lastmile.parcel.milestone

import com.acme.transport.lastmile.redelivery.quota.RedeliveryAssignment
import com.acme.transport.lastmile.redelivery.quota.redeliveryAuditBucket

fun summarizeRedeliveryAssignmentsForMilestone(items: MutableList<RedeliveryAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveParcelMilestonesFromAssignment(items: MutableList<RedeliveryAssignment>): MutableList<ParcelMilestone> {
    val out = mutableListOf<ParcelMilestone>()
    for (item in items) {
        val derived = newParcelMilestone(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileParcelMilestone(item: ParcelMilestone): String = redeliveryAuditBucket(item.projectedVolume())
