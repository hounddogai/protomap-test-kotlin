package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.safety.incident.preference.ScheduledIncidentForecast
import com.acme.transport.safety.incident.preference.legacyIncidentMetricWeight

fun summarizeScheduledIncidentForecastsForRecord(items: MutableList<ScheduledIncidentForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveGlobalNearMissRecordsFromForecast(items: MutableList<ScheduledIncidentForecast>): MutableList<GlobalNearMissRecord> {
    val out = mutableListOf<GlobalNearMissRecord>()
    for (item in items) {
        val derived = newGlobalNearMissRecord(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectGlobalNearMissRecord(item: GlobalNearMissRecord): Double = legacyIncidentMetricWeight(5, true)
