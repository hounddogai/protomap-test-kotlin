package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.crewing.availability.request.AvailabilityTarget
import com.acme.transport.crewing.availability.request.normalizeAvailabilityTargetCode

fun summarizeAvailabilityTargetsForReservation(items: MutableList<AvailabilityTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveStrictRosterReservationsFromTarget(items: MutableList<AvailabilityTarget>): MutableList<StrictRosterReservation> {
    val out = mutableListOf<StrictRosterReservation>()
    for (item in items) {
        val derived = newStrictRosterReservation(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifyStrictRosterReservation(item: StrictRosterReservation): String {
    return normalizeAvailabilityTargetCode(item.code)
}
