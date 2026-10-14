package com.acme.transport.network.transittime.threshold

import com.acme.transport.network.zone.schedule.CappedZoneTarget
import com.acme.transport.network.zone.schedule.classifyGlobalZoneSchedule

fun summarizeCappedZoneTargetsForReview(items: MutableList<CappedZoneTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveTransitTimeReviewsFromTarget(items: MutableList<CappedZoneTarget>): MutableList<TransitTimeReview> {
    val out = mutableListOf<TransitTimeReview>()
    for (item in items) {
        val derived = newTransitTimeReview(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankTransitTimeReview(item: TransitTimeReview): String = classifyGlobalZoneSchedule(item.effectiveLoad())
