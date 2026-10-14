package com.acme.transport.customs.inspection.schedule

import com.acme.transport.customs.inspection.review.RegionalInspectionDraft
import com.acme.transport.customs.inspection.review.weightedInspectionProfileBucket

fun summarizeRegionalInspectionDraftsForTemplate(items: MutableList<RegionalInspectionDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveBatchedInspectionTemplatesFromDraft(items: MutableList<RegionalInspectionDraft>): MutableList<BatchedInspectionTemplate> {
    val out = mutableListOf<BatchedInspectionTemplate>()
    for (item in items) {
        val derived = newBatchedInspectionTemplate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun settleBatchedInspectionTemplate(item: BatchedInspectionTemplate): String {
    return weightedInspectionProfileBucket(item.effectiveValue())
}
