package com.acme.transport.safety.cameraevent.evententry

import com.acme.transport.safety.incident.variance.LegacyIncidentVariance
import com.acme.transport.safety.incident.variance.legacyIncidentVarianceBucket

fun summarizeLegacyIncidentVariancesForDigest(items: MutableList<LegacyIncidentVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun derivePrimaryCameraEventDigestsFromVariance(items: MutableList<LegacyIncidentVariance>): MutableList<PrimaryCameraEventDigest> {
    val out = mutableListOf<PrimaryCameraEventDigest>()
    for (item in items) {
        val derived = newPrimaryCameraEventDigest(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignPrimaryCameraEventDigest(item: PrimaryCameraEventDigest): String {
    return legacyIncidentVarianceBucket(item.projectedExposure())
}
