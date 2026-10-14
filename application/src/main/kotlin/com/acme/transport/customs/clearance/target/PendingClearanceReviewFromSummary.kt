package com.acme.transport.customs.clearance.target

import com.acme.transport.customs.hold.summary.HoldSummary
import com.acme.transport.customs.hold.summary.holdSummaryWeight

fun summarizeHoldSummariesForReview(items: MutableList<HoldSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun derivePendingClearanceReviewsFromSummary(items: MutableList<HoldSummary>): MutableList<PendingClearanceReview> {
    val out = mutableListOf<PendingClearanceReview>()
    for (item in items) {
        val derived = newPendingClearanceReview(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scorePendingClearanceReview(item: PendingClearanceReview): Double = holdSummaryWeight(9, true)
