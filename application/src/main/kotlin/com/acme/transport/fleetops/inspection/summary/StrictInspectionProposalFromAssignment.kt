package com.acme.transport.fleetops.inspection.summary

import com.acme.transport.fleetops.leasereturn.audit.LeaseReturnAssignment
import com.acme.transport.fleetops.leasereturn.audit.lenientLeaseReturnAuditWeight

fun summarizeLeaseReturnAssignmentsForProposal(items: MutableList<LeaseReturnAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveStrictInspectionProposalsFromAssignment(items: MutableList<LeaseReturnAssignment>): MutableList<StrictInspectionProposal> {
    val out = mutableListOf<StrictInspectionProposal>()
    for (item in items) {
        val derived = newStrictInspectionProposal(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyStrictInspectionProposal(item: StrictInspectionProposal): Double = lenientLeaseReturnAuditWeight(4, true)
