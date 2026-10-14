package com.acme.transport.linehaul.trailer.cycle

import com.acme.transport.linehaul.tender.entry.FallbackTenderAssignment
import com.acme.transport.linehaul.tender.entry.pendingTenderProposalBucket

fun summarizeFallbackTenderAssignmentsForMetric(items: MutableList<FallbackTenderAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveManualTrailerMetricsFromAssignment(items: MutableList<FallbackTenderAssignment>): MutableList<ManualTrailerMetric> {
    val out = mutableListOf<ManualTrailerMetric>()
    for (item in items) {
        val derived = newManualTrailerMetric(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planManualTrailerMetric(item: ManualTrailerMetric): String = pendingTenderProposalBucket(item.measuredValue())
