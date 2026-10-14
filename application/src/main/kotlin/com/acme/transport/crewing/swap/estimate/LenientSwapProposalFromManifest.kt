package com.acme.transport.crewing.swap.estimate

import com.acme.transport.crewing.roster.recorditem.ScheduledRosterManifest
import com.acme.transport.crewing.roster.recorditem.lenientRosterLedgerBucket

fun summarizeScheduledRosterManifestsForProposal(items: MutableList<ScheduledRosterManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveLenientSwapProposalsFromManifest(items: MutableList<ScheduledRosterManifest>): MutableList<LenientSwapProposal> {
    val out = mutableListOf<LenientSwapProposal>()
    for (item in items) {
        val derived = newLenientSwapProposal(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyLenientSwapProposal(item: LenientSwapProposal): String = lenientRosterLedgerBucket(item.projectedTotal())
