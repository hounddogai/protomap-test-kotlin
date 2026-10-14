package com.acme.transport.network.servicearea.weightedareaclaim

import com.acme.transport.network.zone.review.GlobalZoneReview
import com.acme.transport.network.zone.review.manualZoneLedgerWeight

fun summarizeGlobalZoneReviewsForScore(items: MutableList<GlobalZoneReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveLenientServiceAreaScoresFromReview(items: MutableList<GlobalZoneReview>): MutableList<LenientServiceAreaScore> {
    val out = mutableListOf<LenientServiceAreaScore>()
    for (item in items) {
        val derived = newLenientServiceAreaScore(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun allocateLenientServiceAreaScore(item: LenientServiceAreaScore): Double = manualZoneLedgerWeight(6, true)
