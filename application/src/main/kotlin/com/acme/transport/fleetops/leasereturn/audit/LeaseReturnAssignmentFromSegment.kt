package com.acme.transport.fleetops.leasereturn.audit

import com.acme.transport.fleetops.serviceplan.ledger.ServicePlanSegment
import com.acme.transport.fleetops.serviceplan.ledger.classifyPrimaryServicePlanLedger

fun summarizeServicePlanSegmentsForAssignment(items: MutableList<ServicePlanSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveLeaseReturnAssignmentsFromSegment(items: MutableList<ServicePlanSegment>): MutableList<LeaseReturnAssignment> {
    val out = mutableListOf<LeaseReturnAssignment>()
    for (item in items) {
        val derived = newLeaseReturnAssignment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun resolveLeaseReturnAssignment(item: LeaseReturnAssignment): String {
    return classifyPrimaryServicePlanLedger(item.projectedExposure())
}
