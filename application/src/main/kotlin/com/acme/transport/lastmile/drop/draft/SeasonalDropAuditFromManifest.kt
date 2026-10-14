package com.acme.transport.lastmile.drop.draft

import com.acme.transport.lastmile.attempt.manifest.AttemptManifest
import com.acme.transport.lastmile.attempt.manifest.classifyAttemptManifest

fun summarizeAttemptManifestsForAudit(items: MutableList<AttemptManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveSeasonalDropAuditsFromManifest(items: MutableList<AttemptManifest>): MutableList<SeasonalDropAudit> {
    val out = mutableListOf<SeasonalDropAudit>()
    for (item in items) {
        val derived = newSeasonalDropAudit(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun allocateSeasonalDropAudit(item: SeasonalDropAudit): String = classifyAttemptManifest(item.weightedVolume())
