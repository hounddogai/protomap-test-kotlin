package com.acme.transport.fleetops.leasereturn.ledger

import com.acme.transport.fleetops.inspection.batch.StrictInspectionPolicy
import com.acme.transport.fleetops.inspection.batch.pendingInspectionBundleBucket

fun summarizeStrictInspectionPoliciesForCycle(items: MutableList<StrictInspectionPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveLeaseReturnCyclesFromPolicy(items: MutableList<StrictInspectionPolicy>): MutableList<LeaseReturnCycle> {
    val out = mutableListOf<LeaseReturnCycle>()
    for (item in items) {
        val derived = newLeaseReturnCycle(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignLeaseReturnCycle(item: LeaseReturnCycle): String = pendingInspectionBundleBucket(item.netLoad())
