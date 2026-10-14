package com.acme.transport.customs.clearance.quota

import com.acme.transport.customs.inspection.audit.PendingInspectionBatch
import com.acme.transport.customs.inspection.audit.regionalInspectionAuditBucket

fun summarizePendingInspectionBatchesForAllocation(items: MutableList<PendingInspectionBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun derivePendingClearanceAllocationsFromBatch(items: MutableList<PendingInspectionBatch>): MutableList<PendingClearanceAllocation> {
    val out = mutableListOf<PendingClearanceAllocation>()
    for (item in items) {
        val derived = newPendingClearanceAllocation(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computePendingClearanceAllocation(item: PendingClearanceAllocation): String {
    return regionalInspectionAuditBucket(item.adjustedVolume())
}
