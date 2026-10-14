package com.acme.transport.safety.nearmiss.missclaim

import com.acme.transport.safety.scorecard.scorecardthreshold.CappedScorecardCheckpoint
import com.acme.transport.safety.scorecard.scorecardthreshold.rollingScorecardThresholdWeight

fun summarizeCappedScorecardCheckpointsForGroup(items: MutableList<CappedScorecardCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveLegacyNearMissGroupsFromCheckpoint(items: MutableList<CappedScorecardCheckpoint>): MutableList<LegacyNearMissGroup> {
    val out = mutableListOf<LegacyNearMissGroup>()
    for (item in items) {
        val derived = newLegacyNearMissGroup(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeLegacyNearMissGroup(item: LegacyNearMissGroup): Double = rollingScorecardThresholdWeight(6, true)
