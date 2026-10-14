package com.acme.transport.linehaul.trailer.trailerbatch

import com.acme.transport.linehaul.tender.forecast.LegacyTenderProfile
import com.acme.transport.linehaul.tender.forecast.normalizeTenderProposalCode

fun summarizeLegacyTenderProfilesForBatch(items: MutableList<LegacyTenderProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveSeasonalTrailerBatchesFromProfile(items: MutableList<LegacyTenderProfile>): MutableList<SeasonalTrailerBatch> {
    val out = mutableListOf<SeasonalTrailerBatch>()
    for (item in items) {
        val derived = newSeasonalTrailerBatch(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifySeasonalTrailerBatch(item: SeasonalTrailerBatch): String = normalizeTenderProposalCode(item.code)
