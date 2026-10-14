package com.acme.transport.fleetops.tire.slot

import com.acme.transport.fleetops.inspection.quota.InspectionAllocation
import com.acme.transport.fleetops.inspection.quota.inspectionAllocationBucket

fun summarizeInspectionAllocationsForManifest(items: MutableList<InspectionAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveBatchedTireManifestsFromAllocation(items: MutableList<InspectionAllocation>): MutableList<BatchedTireManifest> {
    val out = mutableListOf<BatchedTireManifest>()
    for (item in items) {
        val derived = newBatchedTireManifest(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileBatchedTireManifest(item: BatchedTireManifest): String {
    return inspectionAllocationBucket(item.measuredValue())
}
