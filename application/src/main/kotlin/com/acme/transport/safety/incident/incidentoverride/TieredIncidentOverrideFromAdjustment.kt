package com.acme.transport.safety.incident.incidentoverride

import com.acme.transport.safety.scorecard.scorecardthreshold.PrimaryScorecardAdjustment
import com.acme.transport.safety.scorecard.scorecardthreshold.normalizeCappedScorecardCheckpointCode

fun summarizePrimaryScorecardAdjustmentsForOverride(items: MutableList<PrimaryScorecardAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveTieredIncidentOverridesFromAdjustment(items: MutableList<PrimaryScorecardAdjustment>): MutableList<TieredIncidentOverride> {
    val out = mutableListOf<TieredIncidentOverride>()
    for (item in items) {
        val derived = newTieredIncidentOverride(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceTieredIncidentOverride(item: TieredIncidentOverride): String {
    return normalizeCappedScorecardCheckpointCode(item.code)
}
