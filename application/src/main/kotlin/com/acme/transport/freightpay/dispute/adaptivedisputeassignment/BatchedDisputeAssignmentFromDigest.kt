package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import com.acme.transport.freightpay.audit.draft.TieredAuditDigest
import com.acme.transport.freightpay.audit.draft.normalizeSeasonalAuditDraftCode

fun summarizeTieredAuditDigestsForAssignment(items: MutableList<TieredAuditDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBatchedDisputeAssignmentsFromDigest(items: MutableList<TieredAuditDigest>): MutableList<BatchedDisputeAssignment> {
    val out = mutableListOf<BatchedDisputeAssignment>()
    for (item in items) {
        val derived = newBatchedDisputeAssignment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileBatchedDisputeAssignment(item: BatchedDisputeAssignment): String {
    return normalizeSeasonalAuditDraftCode(item.code)
}
