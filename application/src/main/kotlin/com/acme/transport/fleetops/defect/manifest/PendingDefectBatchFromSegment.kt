package com.acme.transport.fleetops.defect.manifest

import com.acme.transport.fleetops.leasereturn.segment.LeaseReturnSegment
import com.acme.transport.fleetops.leasereturn.segment.leaseReturnSegmentBucket

fun summarizeLeaseReturnSegmentsForBatch(items: MutableList<LeaseReturnSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun derivePendingDefectBatchesFromSegment(items: MutableList<LeaseReturnSegment>): MutableList<PendingDefectBatch> {
    val out = mutableListOf<PendingDefectBatch>()
    for (item in items) {
        val derived = newPendingDefectBatch(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizePendingDefectBatch(item: PendingDefectBatch): String = leaseReturnSegmentBucket(item.adjustedTotal())
