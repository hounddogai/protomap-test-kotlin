package com.acme.transport.fleetops.inspection.inspectiongroup

import com.acme.transport.fleetops.defect.manifest.PendingDefectBatch
import com.acme.transport.fleetops.defect.manifest.tieredDefectManifestBucket

fun summarizePendingDefectBatchesForCheckpoint(items: MutableList<PendingDefectBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveRollingInspectionCheckpointsFromBatch(items: MutableList<PendingDefectBatch>): MutableList<RollingInspectionCheckpoint> {
    val out = mutableListOf<RollingInspectionCheckpoint>()
    for (item in items) {
        val derived = newRollingInspectionCheckpoint(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planRollingInspectionCheckpoint(item: RollingInspectionCheckpoint): String {
    return tieredDefectManifestBucket(item.effectiveValue())
}
