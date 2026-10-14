package com.acme.transport.crewing.qualification.overrideitem

import com.acme.transport.crewing.roster.digest.BatchedRosterReview
import com.acme.transport.crewing.roster.digest.rosterBudgetBucket

fun summarizeBatchedRosterReviewsForPolicy(items: MutableList<BatchedRosterReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveQualificationPoliciesFromReview(items: MutableList<BatchedRosterReview>): MutableList<QualificationPolicy> {
    val out = mutableListOf<QualificationPolicy>()
    for (item in items) {
        val derived = newQualificationPolicy(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileQualificationPolicy(item: QualificationPolicy): String = rosterBudgetBucket(item.netVolume())
