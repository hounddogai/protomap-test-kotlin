package com.acme.transport.fleetops.utilization.entry

import com.acme.transport.fleetops.leasereturn.returnpreference.PendingLeaseReturnPreference
import com.acme.transport.fleetops.leasereturn.returnpreference.adaptiveLeaseReturnProposalWeight

fun summarizePendingLeaseReturnPreferencesForPlan(items: MutableList<PendingLeaseReturnPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePrimaryUtilizationPlansFromPreference(items: MutableList<PendingLeaseReturnPreference>): MutableList<PrimaryUtilizationPlan> {
    val out = mutableListOf<PrimaryUtilizationPlan>()
    for (item in items) {
        val derived = newPrimaryUtilizationPlan(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun rankPrimaryUtilizationPlan(item: PrimaryUtilizationPlan): Double = adaptiveLeaseReturnProposalWeight(9, true)
