package com.acme.transport.terminal.traileryard.review

import com.acme.transport.terminal.sortation.group.CappedSortationEstimate
import com.acme.transport.terminal.sortation.group.sortationGroupBucket

fun summarizeCappedSortationEstimatesForReview(items: MutableList<CappedSortationEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveTrailerYardReviewsFromEstimate(items: MutableList<CappedSortationEstimate>): MutableList<TrailerYardReview> {
    val out = mutableListOf<TrailerYardReview>()
    for (item in items) {
        val derived = newTrailerYardReview(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateTrailerYardReview(item: TrailerYardReview): String = sortationGroupBucket(item.measuredValue())
