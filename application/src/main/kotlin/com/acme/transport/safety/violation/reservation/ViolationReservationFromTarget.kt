package com.acme.transport.safety.violation.reservation

import com.acme.transport.safety.scorecard.assignment.FallbackScorecardTarget
import com.acme.transport.safety.scorecard.assignment.fallbackScorecardTargetBucket

fun summarizeFallbackScorecardTargetsForReservation(items: MutableList<FallbackScorecardTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveViolationReservationsFromTarget(items: MutableList<FallbackScorecardTarget>): MutableList<ViolationReservation> {
    val out = mutableListOf<ViolationReservation>()
    for (item in items) {
        val derived = newViolationReservation(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceViolationReservation(item: ViolationReservation): String {
    return fallbackScorecardTargetBucket(item.measuredValue())
}
