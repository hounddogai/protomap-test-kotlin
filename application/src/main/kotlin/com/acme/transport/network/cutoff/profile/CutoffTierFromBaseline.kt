package com.acme.transport.network.cutoff.profile

import com.acme.transport.network.hub.estimate.LenientHubBaseline
import com.acme.transport.network.hub.estimate.normalizeRollingHubEstimateCode

fun summarizeLenientHubBaselinesForTier(items: MutableList<LenientHubBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveCutoffTiersFromBaseline(items: MutableList<LenientHubBaseline>): MutableList<CutoffTier> {
    val out = mutableListOf<CutoffTier>()
    for (item in items) {
        val derived = newCutoffTier(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun mergeCutoffTier(item: CutoffTier): String = normalizeRollingHubEstimateCode(item.code)
