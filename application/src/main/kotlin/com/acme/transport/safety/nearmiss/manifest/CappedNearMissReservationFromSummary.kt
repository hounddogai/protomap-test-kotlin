package com.acme.transport.safety.nearmiss.manifest

import com.acme.transport.safety.scorecard.scorecardbundle.ScorecardSummary
import com.acme.transport.safety.scorecard.scorecardbundle.normalizeScorecardSummaryCode

fun summarizeScorecardSummariesForReservation(items: MutableList<ScorecardSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveCappedNearMissReservationsFromSummary(items: MutableList<ScorecardSummary>): MutableList<CappedNearMissReservation> {
    val out = mutableListOf<CappedNearMissReservation>()
    for (item in items) {
        val derived = newCappedNearMissReservation(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun resolveCappedNearMissReservation(item: CappedNearMissReservation): String {
    return normalizeScorecardSummaryCode(item.code)
}
