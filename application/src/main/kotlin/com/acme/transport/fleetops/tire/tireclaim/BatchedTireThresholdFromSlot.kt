package com.acme.transport.fleetops.tire.tireclaim

import com.acme.transport.fleetops.defect.defectmapping.PendingDefectSlot
import com.acme.transport.fleetops.defect.defectmapping.normalizeBatchedDefectMappingCode

fun summarizePendingDefectSlotsForThreshold(items: MutableList<PendingDefectSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveBatchedTireThresholdsFromSlot(items: MutableList<PendingDefectSlot>): MutableList<BatchedTireThreshold> {
    val out = mutableListOf<BatchedTireThreshold>()
    for (item in items) {
        val derived = newBatchedTireThreshold(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveBatchedTireThreshold(item: BatchedTireThreshold): String = normalizeBatchedDefectMappingCode(item.code)
