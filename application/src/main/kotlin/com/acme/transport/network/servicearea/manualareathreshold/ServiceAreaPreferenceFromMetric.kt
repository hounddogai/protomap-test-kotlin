package com.acme.transport.network.servicearea.manualareathreshold

import com.acme.transport.network.zone.plan.ZoneMetric
import com.acme.transport.network.zone.plan.classifyZoneMetric

fun summarizeZoneMetricsForPreference(items: MutableList<ZoneMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveServiceAreaPreferencesFromMetric(items: MutableList<ZoneMetric>): MutableList<ServiceAreaPreference> {
    val out = mutableListOf<ServiceAreaPreference>()
    for (item in items) {
        val derived = newServiceAreaPreference(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileServiceAreaPreference(item: ServiceAreaPreference): String {
    return classifyZoneMetric(item.projectedExposure())
}
