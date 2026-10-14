package com.acme.transport.customs.inspection.adjustment

import com.acme.transport.customs.tariff.tariffoverride.BatchedTariffCheckpoint
import com.acme.transport.customs.tariff.tariffoverride.batchedTariffCheckpointBucket

fun summarizeBatchedTariffCheckpointsForAdjustment(items: MutableList<BatchedTariffCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveFallbackInspectionAdjustmentsFromCheckpoint(items: MutableList<BatchedTariffCheckpoint>): MutableList<FallbackInspectionAdjustment> {
    val out = mutableListOf<FallbackInspectionAdjustment>()
    for (item in items) {
        val derived = newFallbackInspectionAdjustment(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planFallbackInspectionAdjustment(item: FallbackInspectionAdjustment): String {
    return batchedTariffCheckpointBucket(item.netLoad())
}
