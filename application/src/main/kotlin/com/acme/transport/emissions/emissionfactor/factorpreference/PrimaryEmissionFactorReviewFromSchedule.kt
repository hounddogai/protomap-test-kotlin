package com.acme.transport.emissions.emissionfactor.factorpreference

import com.acme.transport.emissions.idling.review.IdlingSchedule
import com.acme.transport.emissions.idling.review.idlingScheduleWeight

fun summarizeIdlingSchedulesForReview(items: MutableList<IdlingSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun derivePrimaryEmissionFactorReviewsFromSchedule(items: MutableList<IdlingSchedule>): MutableList<PrimaryEmissionFactorReview> {
    val out = mutableListOf<PrimaryEmissionFactorReview>()
    for (item in items) {
        val derived = newPrimaryEmissionFactorReview(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergePrimaryEmissionFactorReview(item: PrimaryEmissionFactorReview): Double = idlingScheduleWeight(9, true)
