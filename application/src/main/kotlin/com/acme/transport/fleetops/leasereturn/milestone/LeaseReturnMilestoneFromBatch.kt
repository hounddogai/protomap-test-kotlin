package com.acme.transport.fleetops.leasereturn.milestone

import com.acme.transport.fleetops.inspection.inspectionbatch.CappedInspectionBatch
import com.acme.transport.fleetops.inspection.inspectionbatch.cappedInspectionBatchWeight

fun summarizeCappedInspectionBatchesForMilestone(items: MutableList<CappedInspectionBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveLeaseReturnMilestonesFromBatch(items: MutableList<CappedInspectionBatch>): MutableList<LeaseReturnMilestone> {
    val out = mutableListOf<LeaseReturnMilestone>()
    for (item in items) {
        val derived = newLeaseReturnMilestone(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeLeaseReturnMilestone(item: LeaseReturnMilestone): Double = cappedInspectionBatchWeight(6, true)
