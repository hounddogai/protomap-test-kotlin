package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.customs.clearance.clearancecheckpoint.BatchedClearanceCheckpoint
import com.acme.transport.customs.clearance.clearancecheckpoint.batchedClearanceCheckpointBucket

fun summarizeBatchedClearanceCheckpointsForScore(items: MutableList<BatchedClearanceCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveTieredDeclarationScoresFromCheckpoint(items: MutableList<BatchedClearanceCheckpoint>): MutableList<TieredDeclarationScore> {
    val out = mutableListOf<TieredDeclarationScore>()
    for (item in items) {
        val derived = newTieredDeclarationScore(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeTieredDeclarationScore(item: TieredDeclarationScore): String {
    return batchedClearanceCheckpointBucket(item.measuredValue())
}
