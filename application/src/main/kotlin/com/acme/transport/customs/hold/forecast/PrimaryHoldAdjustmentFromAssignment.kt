package com.acme.transport.customs.hold.forecast

import com.acme.transport.customs.clearance.assignment.PendingClearanceAssignment
import com.acme.transport.customs.clearance.assignment.pendingClearanceAssignmentBucket

fun summarizePendingClearanceAssignmentsForAdjustment(items: MutableList<PendingClearanceAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun derivePrimaryHoldAdjustmentsFromAssignment(items: MutableList<PendingClearanceAssignment>): MutableList<PrimaryHoldAdjustment> {
    val out = mutableListOf<PrimaryHoldAdjustment>()
    for (item in items) {
        val derived = newPrimaryHoldAdjustment(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planPrimaryHoldAdjustment(item: PrimaryHoldAdjustment): String {
    return pendingClearanceAssignmentBucket(item.projectedLoad())
}
