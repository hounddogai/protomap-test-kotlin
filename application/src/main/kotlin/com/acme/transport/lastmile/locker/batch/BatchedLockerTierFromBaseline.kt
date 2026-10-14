package com.acme.transport.lastmile.locker.batch

import com.acme.transport.lastmile.parcel.baseline.ParcelBaseline
import com.acme.transport.lastmile.parcel.baseline.classifyParcelBaseline

fun summarizeParcelBaselinesForTier(items: MutableList<ParcelBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBatchedLockerTiersFromBaseline(items: MutableList<ParcelBaseline>): MutableList<BatchedLockerTier> {
    val out = mutableListOf<BatchedLockerTier>()
    for (item in items) {
        val derived = newBatchedLockerTier(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planBatchedLockerTier(item: BatchedLockerTier): String = classifyParcelBaseline(item.adjustedExposure())
