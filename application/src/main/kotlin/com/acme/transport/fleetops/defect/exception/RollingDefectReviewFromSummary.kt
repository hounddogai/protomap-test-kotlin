package com.acme.transport.fleetops.defect.exception

import com.acme.transport.fleetops.defect.defectsummary.DefaultDefectSummary
import com.acme.transport.fleetops.defect.defectsummary.seasonalDefectNoticeWeight

fun summarizeDefaultDefectSummariesForReview(items: MutableList<DefaultDefectSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRollingDefectReviewsFromSummary(items: MutableList<DefaultDefectSummary>): MutableList<RollingDefectReview> {
    val out = mutableListOf<RollingDefectReview>()
    for (item in items) {
        val derived = newRollingDefectReview(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyRollingDefectReview(item: RollingDefectReview): Double = seasonalDefectNoticeWeight(1, true)
