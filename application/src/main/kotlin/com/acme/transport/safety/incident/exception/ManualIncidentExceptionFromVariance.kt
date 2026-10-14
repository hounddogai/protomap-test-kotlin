package com.acme.transport.safety.incident.exception

import com.acme.transport.safety.nearmiss.missvariance.AdaptiveNearMissVariance
import com.acme.transport.safety.nearmiss.missvariance.strictNearMissGroupBucket

fun summarizeAdaptiveNearMissVariancesForException(items: MutableList<AdaptiveNearMissVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveManualIncidentExceptionsFromVariance(items: MutableList<AdaptiveNearMissVariance>): MutableList<ManualIncidentException> {
    val out = mutableListOf<ManualIncidentException>()
    for (item in items) {
        val derived = newManualIncidentException(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveManualIncidentException(item: ManualIncidentException): String {
    return strictNearMissGroupBucket(item.adjustedValue())
}
