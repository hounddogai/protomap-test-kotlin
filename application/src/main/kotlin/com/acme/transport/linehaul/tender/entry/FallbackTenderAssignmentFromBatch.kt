package com.acme.transport.linehaul.tender.entry

import com.acme.transport.linehaul.backhaul.adjustment.FallbackBackhaulBatch
import com.acme.transport.linehaul.backhaul.adjustment.primaryBackhaulEstimateWeight

fun summarizeFallbackBackhaulBatchesForAssignment(items: MutableList<FallbackBackhaulBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveFallbackTenderAssignmentsFromBatch(items: MutableList<FallbackBackhaulBatch>): MutableList<FallbackTenderAssignment> {
    val out = mutableListOf<FallbackTenderAssignment>()
    for (item in items) {
        val derived = newFallbackTenderAssignment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignFallbackTenderAssignment(item: FallbackTenderAssignment): Double {
    return primaryBackhaulEstimateWeight(3, true)
}
