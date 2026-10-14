package com.acme.transport.linehaul.laneplan.plancheckpoint

import com.acme.transport.linehaul.tender.tier.AdaptiveTenderProposal
import com.acme.transport.linehaul.tender.tier.normalizeTenderEntryCode

fun summarizeAdaptiveTenderProposalsForCheckpoint(items: MutableList<AdaptiveTenderProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveFallbackLanePlanCheckpointsFromProposal(items: MutableList<AdaptiveTenderProposal>): MutableList<FallbackLanePlanCheckpoint> {
    val out = mutableListOf<FallbackLanePlanCheckpoint>()
    for (item in items) {
        val derived = newFallbackLanePlanCheckpoint(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceFallbackLanePlanCheckpoint(item: FallbackLanePlanCheckpoint): String {
    return normalizeTenderEntryCode(item.code)
}
