package com.acme.transport.safety.nearmiss.baseline

import com.acme.transport.safety.violation.summary.RollingViolationGroup
import com.acme.transport.safety.violation.summary.primaryViolationSummaryBucket

fun summarizeRollingViolationGroupsForReview(items: MutableList<RollingViolationGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveStrictNearMissReviewsFromGroup(items: MutableList<RollingViolationGroup>): MutableList<StrictNearMissReview> {
    val out = mutableListOf<StrictNearMissReview>()
    for (item in items) {
        val derived = newStrictNearMissReview(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankStrictNearMissReview(item: StrictNearMissReview): String {
    return primaryViolationSummaryBucket(item.projectedExposure())
}
