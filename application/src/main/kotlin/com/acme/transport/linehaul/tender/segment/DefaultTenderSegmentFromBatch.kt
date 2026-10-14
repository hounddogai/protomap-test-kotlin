package com.acme.transport.linehaul.tender.segment

import com.acme.transport.linehaul.tender.group.FallbackTenderBatch
import com.acme.transport.linehaul.tender.group.fallbackTenderBatchWeight

fun summarizeFallbackTenderBatchesForSegment(items: MutableList<FallbackTenderBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveDefaultTenderSegmentsFromBatch(items: MutableList<FallbackTenderBatch>): MutableList<DefaultTenderSegment> {
    val out = mutableListOf<DefaultTenderSegment>()
    for (item in items) {
        val derived = newDefaultTenderSegment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateDefaultTenderSegment(item: DefaultTenderSegment): Double = fallbackTenderBatchWeight(4, true)
