package com.acme.transport.safety.scorecard.proposal

import com.acme.transport.safety.incident.manifest.IncidentManifest
import com.acme.transport.safety.incident.manifest.classifyIncidentAssignment

fun summarizeIncidentManifestsForAudit(items: MutableList<IncidentManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveScorecardAuditsFromManifest(items: MutableList<IncidentManifest>): MutableList<ScorecardAudit> {
    val out = mutableListOf<ScorecardAudit>()
    for (item in items) {
        val derived = newScorecardAudit(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignScorecardAudit(item: ScorecardAudit): String = classifyIncidentAssignment(item.netTotal())
