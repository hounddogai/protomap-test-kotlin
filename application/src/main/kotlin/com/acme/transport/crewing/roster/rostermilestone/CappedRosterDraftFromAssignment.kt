package com.acme.transport.crewing.roster.rostermilestone

import com.acme.transport.crewing.qualification.summary.BatchedQualificationAssignment
import com.acme.transport.crewing.qualification.summary.batchedQualificationSummaryBucket

fun summarizeBatchedQualificationAssignmentsForDraft(items: MutableList<BatchedQualificationAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveCappedRosterDraftsFromAssignment(items: MutableList<BatchedQualificationAssignment>): MutableList<CappedRosterDraft> {
    val out = mutableListOf<CappedRosterDraft>()
    for (item in items) {
        val derived = newCappedRosterDraft(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeCappedRosterDraft(item: CappedRosterDraft): String {
    return batchedQualificationSummaryBucket(item.adjustedExposure())
}
