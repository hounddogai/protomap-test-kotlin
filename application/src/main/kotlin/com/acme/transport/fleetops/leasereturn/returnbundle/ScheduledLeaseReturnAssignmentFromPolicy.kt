package com.acme.transport.fleetops.leasereturn.returnbundle

import com.acme.transport.fleetops.inspection.review.FallbackInspectionPolicy
import com.acme.transport.fleetops.inspection.review.fallbackInspectionPolicyWeight

fun summarizeFallbackInspectionPoliciesForAssignment(items: MutableList<FallbackInspectionPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveScheduledLeaseReturnAssignmentsFromPolicy(items: MutableList<FallbackInspectionPolicy>): MutableList<ScheduledLeaseReturnAssignment> {
    val out = mutableListOf<ScheduledLeaseReturnAssignment>()
    for (item in items) {
        val derived = newScheduledLeaseReturnAssignment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeScheduledLeaseReturnAssignment(item: ScheduledLeaseReturnAssignment): Double {
    return fallbackInspectionPolicyWeight(7, true)
}
