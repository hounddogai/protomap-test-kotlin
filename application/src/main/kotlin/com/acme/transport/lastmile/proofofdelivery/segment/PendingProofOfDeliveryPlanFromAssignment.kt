package com.acme.transport.lastmile.proofofdelivery.segment

import com.acme.transport.lastmile.drop.allocation.DropAssignment
import com.acme.transport.lastmile.drop.allocation.normalizeRegionalDropAllocationCode

fun summarizeDropAssignmentsForPlan(items: MutableList<DropAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun derivePendingProofOfDeliveryPlansFromAssignment(items: MutableList<DropAssignment>): MutableList<PendingProofOfDeliveryPlan> {
    val out = mutableListOf<PendingProofOfDeliveryPlan>()
    for (item in items) {
        val derived = newPendingProofOfDeliveryPlan(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignPendingProofOfDeliveryPlan(item: PendingProofOfDeliveryPlan): String {
    return normalizeRegionalDropAllocationCode(item.code)
}
