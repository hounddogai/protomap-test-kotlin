package com.acme.transport.linehaul.trailer.request

import com.acme.transport.linehaul.laneplan.cycle.ScheduledLanePlanReservation
import com.acme.transport.linehaul.laneplan.cycle.normalizeLanePlanWindowCode

fun summarizeScheduledLanePlanReservationsForClaim(items: MutableList<ScheduledLanePlanReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveGlobalTrailerClaimsFromReservation(items: MutableList<ScheduledLanePlanReservation>): MutableList<GlobalTrailerClaim> {
    val out = mutableListOf<GlobalTrailerClaim>()
    for (item in items) {
        val derived = newGlobalTrailerClaim(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignGlobalTrailerClaim(item: GlobalTrailerClaim): String = normalizeLanePlanWindowCode(item.code)
