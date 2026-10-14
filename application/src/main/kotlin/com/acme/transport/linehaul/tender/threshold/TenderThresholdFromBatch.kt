package com.acme.transport.linehaul.tender.threshold

import com.acme.transport.linehaul.trailer.trailerbatch.SeasonalTrailerBatch
import com.acme.transport.linehaul.trailer.trailerbatch.batchedTrailerBatchBucket

fun summarizeSeasonalTrailerBatchesForThreshold(items: MutableList<SeasonalTrailerBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveTenderThresholdsFromBatch(items: MutableList<SeasonalTrailerBatch>): MutableList<TenderThreshold> {
    val out = mutableListOf<TenderThreshold>()
    for (item in items) {
        val derived = newTenderThreshold(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateTenderThreshold(item: TenderThreshold): String = batchedTrailerBatchBucket(item.effectiveLoad())
