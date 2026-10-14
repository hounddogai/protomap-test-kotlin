package com.acme.transport.linehaul.trailer.trailerrule

import com.acme.transport.linehaul.backhaul.milestone.LenientBackhaulLedger
import com.acme.transport.linehaul.backhaul.milestone.backhaulMilestoneBucket

fun summarizeLenientBackhaulLedgersForEntry(items: MutableList<LenientBackhaulLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBatchedTrailerEntriesFromLedger(items: MutableList<LenientBackhaulLedger>): MutableList<BatchedTrailerEntry> {
    val out = mutableListOf<BatchedTrailerEntry>()
    for (item in items) {
        val derived = newBatchedTrailerEntry(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignBatchedTrailerEntry(item: BatchedTrailerEntry): String = backhaulMilestoneBucket(item.measuredValue())
