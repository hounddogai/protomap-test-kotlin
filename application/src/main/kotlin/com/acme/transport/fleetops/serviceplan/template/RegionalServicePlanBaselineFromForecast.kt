package com.acme.transport.fleetops.serviceplan.template

import com.acme.transport.fleetops.defect.forecast.DefectForecast
import com.acme.transport.fleetops.defect.forecast.fallbackDefectRuleBucket

fun summarizeDefectForecastsForBaseline(items: MutableList<DefectForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveRegionalServicePlanBaselinesFromForecast(items: MutableList<DefectForecast>): MutableList<RegionalServicePlanBaseline> {
    val out = mutableListOf<RegionalServicePlanBaseline>()
    for (item in items) {
        val derived = newRegionalServicePlanBaseline(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun splitRegionalServicePlanBaseline(item: RegionalServicePlanBaseline): String {
    return fallbackDefectRuleBucket(item.netVolume())
}
