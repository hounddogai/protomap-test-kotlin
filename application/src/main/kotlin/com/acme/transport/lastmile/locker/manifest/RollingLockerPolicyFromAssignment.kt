package com.acme.transport.lastmile.locker.manifest

import com.acme.transport.lastmile.parcel.ledger.ParcelAssignment
import com.acme.transport.lastmile.parcel.ledger.normalizeParcelAssignmentCode

fun summarizeParcelAssignmentsForPolicy(items: MutableList<ParcelAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveRollingLockerPoliciesFromAssignment(items: MutableList<ParcelAssignment>): MutableList<RollingLockerPolicy> {
    val out = mutableListOf<RollingLockerPolicy>()
    for (item in items) {
        val derived = newRollingLockerPolicy(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeRollingLockerPolicy(item: RollingLockerPolicy): String = normalizeParcelAssignmentCode(item.code)
