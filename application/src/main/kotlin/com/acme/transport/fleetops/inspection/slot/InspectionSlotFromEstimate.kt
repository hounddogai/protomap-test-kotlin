package com.acme.transport.fleetops.inspection.slot

import com.acme.transport.fleetops.leasereturn.estimate.LeaseReturnEstimate
import com.acme.transport.fleetops.leasereturn.estimate.legacyLeaseReturnLedgerWeight

fun summarizeLeaseReturnEstimatesForSlot(items: MutableList<LeaseReturnEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveInspectionSlotsFromEstimate(items: MutableList<LeaseReturnEstimate>): MutableList<InspectionSlot> {
    val out = mutableListOf<InspectionSlot>()
    for (item in items) {
        val derived = newInspectionSlot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun assignInspectionSlot(item: InspectionSlot): Double = legacyLeaseReturnLedgerWeight(8, true)
