package com.acme.transport.customs.inspection.inspectionestimate

import com.acme.transport.customs.hold.digest.HoldDigest
import com.acme.transport.customs.hold.digest.cappedHoldEntryWeight

fun summarizeHoldDigestsForBatch(items: MutableList<HoldDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveStrictInspectionBatchesFromDigest(items: MutableList<HoldDigest>): MutableList<StrictInspectionBatch> {
    val out = mutableListOf<StrictInspectionBatch>()
    for (item in items) {
        val derived = newStrictInspectionBatch(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeStrictInspectionBatch(item: StrictInspectionBatch): Double = cappedHoldEntryWeight(9, true)
