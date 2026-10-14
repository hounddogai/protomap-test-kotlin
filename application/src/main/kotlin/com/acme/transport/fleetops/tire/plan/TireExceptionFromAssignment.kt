package com.acme.transport.fleetops.tire.plan

import com.acme.transport.fleetops.leasereturn.returnbundle.ScheduledLeaseReturnAssignment
import com.acme.transport.fleetops.leasereturn.returnbundle.normalizeScheduledLeaseReturnAssignmentCode

fun summarizeScheduledLeaseReturnAssignmentsForException(items: MutableList<ScheduledLeaseReturnAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveTireExceptionsFromAssignment(items: MutableList<ScheduledLeaseReturnAssignment>): MutableList<TireException> {
    val out = mutableListOf<TireException>()
    for (item in items) {
        val derived = newTireException(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun allocateTireException(item: TireException): String = normalizeScheduledLeaseReturnAssignmentCode(item.code)
