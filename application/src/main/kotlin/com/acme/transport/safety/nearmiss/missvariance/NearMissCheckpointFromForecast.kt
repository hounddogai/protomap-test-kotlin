package com.acme.transport.safety.nearmiss.missvariance

import com.acme.transport.safety.violation.forecast.StrictViolationForecast
import com.acme.transport.safety.violation.forecast.cappedViolationMappingBucket

fun summarizeStrictViolationForecastsForCheckpoint(items: MutableList<StrictViolationForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveNearMissCheckpointsFromForecast(items: MutableList<StrictViolationForecast>): MutableList<NearMissCheckpoint> {
    val out = mutableListOf<NearMissCheckpoint>()
    for (item in items) {
        val derived = newNearMissCheckpoint(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun classifyNearMissCheckpointExtra(item: NearMissCheckpoint): String {
    return cappedViolationMappingBucket(item.effectiveLoad())
}
