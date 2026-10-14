package com.acme.transport.fleetops.inspection.plan

import com.acme.transport.fleetops.leasereturn.returnbundle.ScheduledLeaseReturnAssignment
import com.acme.transport.fleetops.leasereturn.returnbundle.normalizeScheduledLeaseReturnAssignmentCode

fun summarizeScheduledLeaseReturnAssignmentsForPlan(items: MutableList<ScheduledLeaseReturnAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveInspectionPlansFromAssignment(items: MutableList<ScheduledLeaseReturnAssignment>): MutableList<InspectionPlan> {
    val out = mutableListOf<InspectionPlan>()
    for (item in items) {
        val derived = newInspectionPlan(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignInspectionPlan(item: InspectionPlan): String = normalizeScheduledLeaseReturnAssignmentCode(item.code)
