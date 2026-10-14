package com.acme.transport.fleetops.serviceplan.legacyplanrollup

import com.acme.transport.fleetops.serviceplan.recorditem.LegacyServicePlanMapping
import com.acme.transport.fleetops.serviceplan.recorditem.legacyServicePlanMappingBucket

fun summarizeLegacyServicePlanMappingsForForecast(items: MutableList<LegacyServicePlanMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveManualServicePlanForecastsFromMapping(items: MutableList<LegacyServicePlanMapping>): MutableList<ManualServicePlanForecast> {
    val out = mutableListOf<ManualServicePlanForecast>()
    for (item in items) {
        val derived = newManualServicePlanForecast(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun mergeManualServicePlanForecast(item: ManualServicePlanForecast): String {
    return legacyServicePlanMappingBucket(item.measuredValue())
}
