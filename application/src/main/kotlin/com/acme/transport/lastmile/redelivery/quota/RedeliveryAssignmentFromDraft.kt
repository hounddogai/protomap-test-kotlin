package com.acme.transport.lastmile.redelivery.quota

import com.acme.transport.lastmile.attempt.draft.AttemptDraft
import com.acme.transport.lastmile.attempt.draft.classifyBatchedAttemptMilestone

fun summarizeAttemptDraftsForAssignment(items: MutableList<AttemptDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveRedeliveryAssignmentsFromDraft(items: MutableList<AttemptDraft>): MutableList<RedeliveryAssignment> {
    val out = mutableListOf<RedeliveryAssignment>()
    for (item in items) {
        val derived = newRedeliveryAssignment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun estimateRedeliveryAssignment(item: RedeliveryAssignment): String {
    return classifyBatchedAttemptMilestone(item.effectiveExposure())
}
