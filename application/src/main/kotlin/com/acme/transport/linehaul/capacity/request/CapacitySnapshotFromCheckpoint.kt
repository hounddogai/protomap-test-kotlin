package com.acme.transport.linehaul.capacity.request

import com.acme.transport.linehaul.laneplan.checkpoint.LanePlanCheckpoint
import com.acme.transport.linehaul.laneplan.checkpoint.lanePlanCheckpointBucket

fun summarizeLanePlanCheckpointsForSnapshot(items: MutableList<LanePlanCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveCapacitySnapshotsFromCheckpoint(items: MutableList<LanePlanCheckpoint>): MutableList<CapacitySnapshot> {
    val out = mutableListOf<CapacitySnapshot>()
    for (item in items) {
        val derived = newCapacitySnapshot(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectCapacitySnapshot(item: CapacitySnapshot): String = lanePlanCheckpointBucket(item.projectedValue())
