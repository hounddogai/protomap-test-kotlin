package com.acme.transport.freightpay.accessorial.entry

import com.acme.transport.freightpay.audit.notice.RegionalAuditSlot
import com.acme.transport.freightpay.audit.notice.regionalAuditSlotBucket

fun summarizeRegionalAuditSlotsForAllocation(items: MutableList<RegionalAuditSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveRollingAccessorialAllocationsFromSlot(items: MutableList<RegionalAuditSlot>): MutableList<RollingAccessorialAllocation> {
    val out = mutableListOf<RollingAccessorialAllocation>()
    for (item in items) {
        val derived = newRollingAccessorialAllocation(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun deriveRollingAccessorialAllocation(item: RollingAccessorialAllocation): String {
    return regionalAuditSlotBucket(item.measuredValue())
}
