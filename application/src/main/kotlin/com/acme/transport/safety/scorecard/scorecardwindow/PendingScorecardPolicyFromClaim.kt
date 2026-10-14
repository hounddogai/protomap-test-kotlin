package com.acme.transport.safety.scorecard.scorecardwindow

import com.acme.transport.safety.incident.variance.RegionalIncidentClaim
import com.acme.transport.safety.incident.variance.legacyIncidentVarianceBucket

fun summarizeRegionalIncidentClaimsForPolicy(items: MutableList<RegionalIncidentClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun derivePendingScorecardPoliciesFromClaim(items: MutableList<RegionalIncidentClaim>): MutableList<PendingScorecardPolicy> {
    val out = mutableListOf<PendingScorecardPolicy>()
    for (item in items) {
        val derived = newPendingScorecardPolicy(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun settlePendingScorecardPolicy(item: PendingScorecardPolicy): String {
    return legacyIncidentVarianceBucket(item.projectedTotal())
}
