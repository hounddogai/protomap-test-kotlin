package com.acme.transport.safety.incident.snapshot

import com.acme.transport.safety.scorecard.milestone.PrimaryScorecardTarget
import com.acme.transport.safety.scorecard.milestone.scorecardMilestoneBucket

fun summarizePrimaryScorecardTargetsForSnapshot(items: MutableList<PrimaryScorecardTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveIncidentSnapshotsFromTarget(items: MutableList<PrimaryScorecardTarget>): MutableList<IncidentSnapshot> {
    val out = mutableListOf<IncidentSnapshot>()
    for (item in items) {
        val derived = newIncidentSnapshot(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveIncidentSnapshot(item: IncidentSnapshot): String = scorecardMilestoneBucket(item.weightedVolume())
