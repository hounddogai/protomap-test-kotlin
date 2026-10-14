package com.acme.transport.safety.nearmiss.missrule

import com.acme.transport.safety.incident.snapshot.StrictIncidentSnapshot
import com.acme.transport.safety.incident.snapshot.incidentEntryBucket

fun summarizeStrictIncidentSnapshotsForQuota(items: MutableList<StrictIncidentSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveAdaptiveNearMissQuotasFromSnapshot(items: MutableList<StrictIncidentSnapshot>): MutableList<AdaptiveNearMissQuota> {
    val out = mutableListOf<AdaptiveNearMissQuota>()
    for (item in items) {
        val derived = newAdaptiveNearMissQuota(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun resolveAdaptiveNearMissQuota(item: AdaptiveNearMissQuota): String = incidentEntryBucket(item.projectedValue())
