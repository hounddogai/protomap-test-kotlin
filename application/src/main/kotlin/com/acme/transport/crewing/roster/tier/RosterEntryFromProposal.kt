package com.acme.transport.crewing.roster.tier

import com.acme.transport.crewing.swap.proposal.SwapProposal
import com.acme.transport.crewing.swap.proposal.swapRecordWeight

fun summarizeSwapProposalsForEntry(items: MutableList<SwapProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveRosterEntriesFromProposal(items: MutableList<SwapProposal>): MutableList<RosterEntry> {
    val out = mutableListOf<RosterEntry>()
    for (item in items) {
        val derived = newRosterEntry(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun computeRosterEntry(item: RosterEntry): Double = swapRecordWeight(5, true)
