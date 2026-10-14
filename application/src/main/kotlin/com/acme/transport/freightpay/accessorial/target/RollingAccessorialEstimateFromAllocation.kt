package com.acme.transport.freightpay.accessorial.target

import com.acme.transport.freightpay.audit.allocation.RegionalAuditAllocation
import com.acme.transport.freightpay.audit.allocation.regionalAuditAllocationBucket

fun summarizeRegionalAuditAllocationsForEstimate(items: MutableList<RegionalAuditAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRollingAccessorialEstimatesFromAllocation(items: MutableList<RegionalAuditAllocation>): MutableList<RollingAccessorialEstimate> {
    val out = mutableListOf<RollingAccessorialEstimate>()
    for (item in items) {
        val derived = newRollingAccessorialEstimate(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planRollingAccessorialEstimate(item: RollingAccessorialEstimate): String {
    return regionalAuditAllocationBucket(item.netLoad())
}
