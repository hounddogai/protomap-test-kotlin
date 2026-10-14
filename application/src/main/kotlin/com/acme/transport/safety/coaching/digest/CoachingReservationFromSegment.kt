package com.acme.transport.safety.coaching.digest

import com.acme.transport.safety.scorecard.segment.StrictScorecardSegment
import com.acme.transport.safety.scorecard.segment.manualScorecardAuditBucket

fun summarizeStrictScorecardSegmentsForReservation(items: MutableList<StrictScorecardSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveCoachingReservationsFromSegment(items: MutableList<StrictScorecardSegment>): MutableList<CoachingReservation> {
    val out = mutableListOf<CoachingReservation>()
    for (item in items) {
        val derived = newCoachingReservation(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyCoachingReservationExtra(item: CoachingReservation): String {
    return manualScorecardAuditBucket(item.effectiveValue())
}
