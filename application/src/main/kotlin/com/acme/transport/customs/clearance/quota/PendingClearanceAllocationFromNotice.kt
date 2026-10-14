package com.acme.transport.customs.clearance.quota

import com.acme.transport.customs.clearance.request.DefaultClearanceNotice
import com.acme.transport.customs.clearance.request.normalizeDefaultClearanceNoticeCode

fun summarizeDefaultClearanceNoticesForAllocation(items: MutableList<DefaultClearanceNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun derivePendingClearanceAllocationsFromNotice(items: MutableList<DefaultClearanceNotice>): MutableList<PendingClearanceAllocation> {
    val out = mutableListOf<PendingClearanceAllocation>()
    for (item in items) {
        val derived = newPendingClearanceAllocation(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balancePendingClearanceAllocation(item: PendingClearanceAllocation): String {
    return normalizeDefaultClearanceNoticeCode(item.code)
}
