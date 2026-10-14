package com.acme.transport.safety.incident.bundle

import com.acme.transport.safety.incident.incidentclaim.AdaptiveIncidentClaim
import com.acme.transport.safety.incident.incidentclaim.normalizeIncidentEstimateCode

fun summarizeAdaptiveIncidentClaimsForRequest(items: MutableList<AdaptiveIncidentClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLegacyIncidentRequestsFromClaim(items: MutableList<AdaptiveIncidentClaim>): MutableList<LegacyIncidentRequest> {
    val out = mutableListOf<LegacyIncidentRequest>()
    for (item in items) {
        val derived = newLegacyIncidentRequest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeLegacyIncidentRequest(item: LegacyIncidentRequest): String = normalizeIncidentEstimateCode(item.code)
