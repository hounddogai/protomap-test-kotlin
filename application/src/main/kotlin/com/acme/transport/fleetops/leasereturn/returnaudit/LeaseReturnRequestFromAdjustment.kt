package com.acme.transport.fleetops.leasereturn.returnaudit

import com.acme.transport.fleetops.utilization.recorditem.UtilizationAdjustment
import com.acme.transport.fleetops.utilization.recorditem.normalizeUtilizationRecordCode

fun summarizeUtilizationAdjustmentsForRequest(items: MutableList<UtilizationAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveLeaseReturnRequestsFromAdjustment(items: MutableList<UtilizationAdjustment>): MutableList<LeaseReturnRequest> {
    val out = mutableListOf<LeaseReturnRequest>()
    for (item in items) {
        val derived = newLeaseReturnRequest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeLeaseReturnRequest(item: LeaseReturnRequest): String = normalizeUtilizationRecordCode(item.code)
