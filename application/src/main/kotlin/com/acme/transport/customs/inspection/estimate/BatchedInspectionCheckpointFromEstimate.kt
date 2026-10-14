package com.acme.transport.customs.inspection.estimate

import com.acme.transport.customs.inspection.audit.GlobalInspectionEstimate
import com.acme.transport.customs.inspection.audit.regionalInspectionAuditBucket

fun summarizeGlobalInspectionEstimatesForCheckpoint(items: MutableList<GlobalInspectionEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBatchedInspectionCheckpointsFromEstimate(items: MutableList<GlobalInspectionEstimate>): MutableList<BatchedInspectionCheckpoint> {
    val out = mutableListOf<BatchedInspectionCheckpoint>()
    for (item in items) {
        val derived = newBatchedInspectionCheckpoint(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceBatchedInspectionCheckpoint(item: BatchedInspectionCheckpoint): String {
    return regionalInspectionAuditBucket(item.weightedVolume())
}
