package com.acme.transport.lastmile.drop.snapshot

import com.acme.transport.lastmile.parcel.rollup.SeasonalParcelProposal
import com.acme.transport.lastmile.parcel.rollup.classifySeasonalParcelProposal

fun summarizeSeasonalParcelProposalsForTier(items: MutableList<SeasonalParcelProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveStrictDropTiersFromProposal(items: MutableList<SeasonalParcelProposal>): MutableList<StrictDropTier> {
    val out = mutableListOf<StrictDropTier>()
    for (item in items) {
        val derived = newStrictDropTier(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitStrictDropTier(item: StrictDropTier): String = classifySeasonalParcelProposal(item.weightedLoad())
