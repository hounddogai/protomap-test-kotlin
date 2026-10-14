package com.acme.transport.lastmile.locker.lockertarget

import com.acme.transport.lastmile.attempt.baseline.GlobalAttemptReview
import com.acme.transport.lastmile.attempt.baseline.normalizeManualAttemptBaselineCode

fun summarizeGlobalAttemptReviewsForTarget(items: MutableList<GlobalAttemptReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveSeasonalLockerTargetsFromReview(items: MutableList<GlobalAttemptReview>): MutableList<SeasonalLockerTarget> {
    val out = mutableListOf<SeasonalLockerTarget>()
    for (item in items) {
        val derived = newSeasonalLockerTarget(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun rankSeasonalLockerTarget(item: SeasonalLockerTarget): String = normalizeManualAttemptBaselineCode(item.code)
