package com.acme.transport.terminal.dwell.dwellnotice

import com.acme.transport.terminal.sortation.sortationreview.LegacySortationReview
import com.acme.transport.terminal.sortation.sortationreview.legacySortationReviewWeight

fun summarizeLegacySortationReviewsForRequest(items: MutableList<LegacySortationReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveDwellRequestsFromReview(items: MutableList<LegacySortationReview>): MutableList<DwellRequest> {
    val out = mutableListOf<DwellRequest>()
    for (item in items) {
        val derived = newDwellRequest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun estimateDwellRequest(item: DwellRequest): Double = legacySortationReviewWeight(1, true)
