package com.acme.transport.lastmile.parcel.rollup

import com.acme.transport.lastmile.parcel.claim.PendingParcelOverride
import com.acme.transport.lastmile.parcel.claim.classifyParcelClaim

fun summarizePendingParcelOverridesForProposal(items: MutableList<PendingParcelOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveSeasonalParcelProposalsFromOverride(items: MutableList<PendingParcelOverride>): MutableList<SeasonalParcelProposal> {
    val out = mutableListOf<SeasonalParcelProposal>()
    for (item in items) {
        val derived = newSeasonalParcelProposal(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun assignSeasonalParcelProposal(item: SeasonalParcelProposal): String {
    return classifyParcelClaim(item.effectiveValue())
}
