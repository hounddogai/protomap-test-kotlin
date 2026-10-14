package com.acme.transport.crewing.swap.score

import com.acme.transport.crewing.roster.summary.RosterProposal
import com.acme.transport.crewing.roster.summary.normalizeRosterProposalCode

fun summarizeRosterProposalsForReservation(items: MutableList<RosterProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveSwapReservationsFromProposal(items: MutableList<RosterProposal>): MutableList<SwapReservation> {
    val out = mutableListOf<SwapReservation>()
    for (item in items) {
        val derived = newSwapReservation(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun applySwapReservation(item: SwapReservation): String = normalizeRosterProposalCode(item.code)
