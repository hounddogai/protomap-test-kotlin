package com.acme.transport.terminal.sortation.window

import com.acme.transport.terminal.traileryard.profile.PendingTrailerYardAdjustment
import com.acme.transport.terminal.traileryard.profile.classifyBatchedTrailerYardPolicy

fun summarizePendingTrailerYardAdjustmentsForBatch(items: MutableList<PendingTrailerYardAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun derivePendingSortationBatchesFromAdjustment(items: MutableList<PendingTrailerYardAdjustment>): MutableList<PendingSortationBatch> {
    val out = mutableListOf<PendingSortationBatch>()
    for (item in items) {
        val derived = newPendingSortationBatch(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimatePendingSortationBatch(item: PendingSortationBatch): String {
    return classifyBatchedTrailerYardPolicy(item.effectiveExposure())
}
