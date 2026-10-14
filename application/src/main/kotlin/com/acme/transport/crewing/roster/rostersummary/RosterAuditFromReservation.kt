package com.acme.transport.crewing.roster.rostersummary

import com.acme.transport.crewing.swap.score.SwapReservation
import com.acme.transport.crewing.swap.score.swapScoreBucket

fun summarizeSwapReservationsForAudit(items: MutableList<SwapReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveRosterAuditsFromReservation(items: MutableList<SwapReservation>): MutableList<RosterAudit> {
    val out = mutableListOf<RosterAudit>()
    for (item in items) {
        val derived = newRosterAudit(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeRosterAudit(item: RosterAudit): String = swapScoreBucket(item.measuredValue())
