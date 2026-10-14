package com.acme.transport.fleetops.leasereturn.audit

import com.acme.transport.fleetops.utilization.segment.CappedUtilizationBudget
import com.acme.transport.fleetops.utilization.segment.normalizeGlobalUtilizationScheduleCode

fun summarizeCappedUtilizationBudgetsForLedger(items: MutableList<CappedUtilizationBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveScheduledLeaseReturnLedgersFromBudget(items: MutableList<CappedUtilizationBudget>): MutableList<ScheduledLeaseReturnLedger> {
    val out = mutableListOf<ScheduledLeaseReturnLedger>()
    for (item in items) {
        val derived = newScheduledLeaseReturnLedger(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateScheduledLeaseReturnLedger(item: ScheduledLeaseReturnLedger): String {
    return normalizeGlobalUtilizationScheduleCode(item.code)
}
