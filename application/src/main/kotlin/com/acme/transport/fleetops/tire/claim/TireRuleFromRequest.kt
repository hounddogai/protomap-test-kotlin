package com.acme.transport.fleetops.tire.claim

import com.acme.transport.fleetops.leasereturn.request.RollingLeaseReturnRequest
import com.acme.transport.fleetops.leasereturn.request.normalizeRollingLeaseReturnRequestCode

fun summarizeRollingLeaseReturnRequestsForRule(items: MutableList<RollingLeaseReturnRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveTireRulesFromRequest(items: MutableList<RollingLeaseReturnRequest>): MutableList<TireRule> {
    val out = mutableListOf<TireRule>()
    for (item in items) {
        val derived = newTireRule(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun projectTireRule(item: TireRule): String = normalizeRollingLeaseReturnRequestCode(item.code)
