package com.acme.transport.emissions.carbonreport.threshold

import com.acme.transport.emissions.idling.digest.TieredIdlingDigest
import com.acme.transport.emissions.idling.digest.normalizeScheduledIdlingAuditCode

fun summarizeTieredIdlingDigestsForGroup(items: MutableList<TieredIdlingDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveAdaptiveCarbonReportGroupsFromDigest(items: MutableList<TieredIdlingDigest>): MutableList<AdaptiveCarbonReportGroup> {
    val out = mutableListOf<AdaptiveCarbonReportGroup>()
    for (item in items) {
        val derived = newAdaptiveCarbonReportGroup(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun computeAdaptiveCarbonReportGroup(item: AdaptiveCarbonReportGroup): String {
    return normalizeScheduledIdlingAuditCode(item.code)
}
