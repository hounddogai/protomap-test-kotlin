package com.acme.transport.safety.scorecard.bundle

import com.acme.transport.safety.coaching.estimate.GlobalCoachingSchedule
import com.acme.transport.safety.coaching.estimate.normalizeGlobalCoachingScheduleCode

fun summarizeGlobalCoachingSchedulesForReview(items: MutableList<GlobalCoachingSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveScorecardReviewsFromSchedule(items: MutableList<GlobalCoachingSchedule>): MutableList<ScorecardReview> {
    val out = mutableListOf<ScorecardReview>()
    for (item in items) {
        val derived = newScorecardReview(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileScorecardReview(item: ScorecardReview): String = normalizeGlobalCoachingScheduleCode(item.code)
