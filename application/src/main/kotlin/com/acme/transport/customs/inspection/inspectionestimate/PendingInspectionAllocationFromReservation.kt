package com.acme.transport.customs.inspection.inspectionestimate

import com.acme.transport.customs.clearance.seasonalclearanceoverride.ClearanceReservation
import com.acme.transport.customs.clearance.seasonalclearanceoverride.clearanceReservationBucket

fun summarizeClearanceReservationsForAllocation(items: MutableList<ClearanceReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun derivePendingInspectionAllocationsFromReservation(items: MutableList<ClearanceReservation>): MutableList<PendingInspectionAllocation> {
    val out = mutableListOf<PendingInspectionAllocation>()
    for (item in items) {
        val derived = newPendingInspectionAllocation(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectPendingInspectionAllocation(item: PendingInspectionAllocation): String {
    return clearanceReservationBucket(item.measuredValue())
}
