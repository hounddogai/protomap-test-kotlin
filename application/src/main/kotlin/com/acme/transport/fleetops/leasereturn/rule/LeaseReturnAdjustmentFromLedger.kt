package com.acme.transport.fleetops.leasereturn.rule

import com.acme.transport.fleetops.serviceplan.quota.ServicePlanLedger
import com.acme.transport.fleetops.serviceplan.quota.classifyLenientServicePlanQuota

fun summarizeServicePlanLedgersForAdjustment(items: MutableList<ServicePlanLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveLeaseReturnAdjustmentsFromLedger(items: MutableList<ServicePlanLedger>): MutableList<LeaseReturnAdjustment> {
    val out = mutableListOf<LeaseReturnAdjustment>()
    for (item in items) {
        val derived = newLeaseReturnAdjustment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectLeaseReturnAdjustment(item: LeaseReturnAdjustment): String {
    return classifyLenientServicePlanQuota(item.netLoad())
}
