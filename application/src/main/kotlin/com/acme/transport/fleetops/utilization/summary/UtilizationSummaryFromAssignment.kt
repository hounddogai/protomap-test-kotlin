package com.acme.transport.fleetops.utilization.summary

import com.acme.transport.fleetops.leasereturn.audit.LeaseReturnAssignment
import com.acme.transport.fleetops.leasereturn.audit.lenientLeaseReturnAuditWeight

fun summarizeLeaseReturnAssignmentsForSummary(items: MutableList<LeaseReturnAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveUtilizationSummariesFromAssignment(items: MutableList<LeaseReturnAssignment>): MutableList<UtilizationSummary> {
    val out = mutableListOf<UtilizationSummary>()
    for (item in items) {
        val derived = newUtilizationSummary(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeUtilizationSummary(item: UtilizationSummary): Double = lenientLeaseReturnAuditWeight(9, true)
