package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.freightpay.audit.recorditem.PendingAuditRecord
import com.acme.transport.freightpay.audit.recorditem.classifyLenientAuditEstimate

fun summarizePendingAuditRecordsForAllocation(items: MutableList<PendingAuditRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveLenientAccessorialAllocationsFromRecord(items: MutableList<PendingAuditRecord>): MutableList<LenientAccessorialAllocation> {
    val out = mutableListOf<LenientAccessorialAllocation>()
    for (item in items) {
        val derived = newLenientAccessorialAllocation(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scoreLenientAccessorialAllocation(item: LenientAccessorialAllocation): String {
    return classifyLenientAuditEstimate(item.measuredValue())
}
