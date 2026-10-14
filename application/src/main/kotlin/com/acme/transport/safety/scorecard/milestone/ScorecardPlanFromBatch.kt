package com.acme.transport.safety.scorecard.milestone

import com.acme.transport.safety.coaching.target.CoachingBatch
import com.acme.transport.safety.coaching.target.coachingBatchBucket

fun summarizeCoachingBatchesForPlan(items: MutableList<CoachingBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveScorecardPlansFromBatch(items: MutableList<CoachingBatch>): MutableList<ScorecardPlan> {
    val out = mutableListOf<ScorecardPlan>()
    for (item in items) {
        val derived = newScorecardPlan(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun matchScorecardPlan(item: ScorecardPlan): String = coachingBatchBucket(item.effectiveValue())
