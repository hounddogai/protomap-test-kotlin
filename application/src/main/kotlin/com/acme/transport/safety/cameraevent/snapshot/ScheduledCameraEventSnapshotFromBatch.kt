package com.acme.transport.safety.cameraevent.snapshot

import com.acme.transport.safety.violation.batch.CappedViolationBatch
import com.acme.transport.safety.violation.batch.batchedViolationDraftBucket

fun summarizeCappedViolationBatchesForSnapshot(items: MutableList<CappedViolationBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveScheduledCameraEventSnapshotsFromBatch(items: MutableList<CappedViolationBatch>): MutableList<ScheduledCameraEventSnapshot> {
    val out = mutableListOf<ScheduledCameraEventSnapshot>()
    for (item in items) {
        val derived = newScheduledCameraEventSnapshot(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeScheduledCameraEventSnapshot(item: ScheduledCameraEventSnapshot): String {
    return batchedViolationDraftBucket(item.weightedExposure())
}
