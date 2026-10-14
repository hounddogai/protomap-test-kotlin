package com.acme.transport.linehaul.tender.fallbacktenderbatch

import com.acme.transport.linehaul.trailer.threshold.TrailerCheckpoint
import com.acme.transport.linehaul.trailer.threshold.trailerCheckpointBucket

fun summarizeTrailerCheckpointsForBatch(items: MutableList<TrailerCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveScheduledTenderBatchesFromCheckpoint(items: MutableList<TrailerCheckpoint>): MutableList<ScheduledTenderBatch> {
    val out = mutableListOf<ScheduledTenderBatch>()
    for (item in items) {
        val derived = newScheduledTenderBatch(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveScheduledTenderBatch(item: ScheduledTenderBatch): String = trailerCheckpointBucket(item.netTotal())
