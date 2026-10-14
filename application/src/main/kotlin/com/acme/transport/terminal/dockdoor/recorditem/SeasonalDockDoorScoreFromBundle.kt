package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.terminal.traileryard.policy.RollingTrailerYardBundle
import com.acme.transport.terminal.traileryard.policy.classifyGlobalTrailerYardPolicy

fun summarizeRollingTrailerYardBundlesForScore(items: MutableList<RollingTrailerYardBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveSeasonalDockDoorScoresFromBundle(items: MutableList<RollingTrailerYardBundle>): MutableList<SeasonalDockDoorScore> {
    val out = mutableListOf<SeasonalDockDoorScore>()
    for (item in items) {
        val derived = newSeasonalDockDoorScore(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileSeasonalDockDoorScore(item: SeasonalDockDoorScore): String {
    return classifyGlobalTrailerYardPolicy(item.weightedVolume())
}
