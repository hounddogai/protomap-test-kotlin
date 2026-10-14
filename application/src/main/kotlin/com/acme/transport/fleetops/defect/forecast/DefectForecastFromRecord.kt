package com.acme.transport.fleetops.defect.forecast

import com.acme.transport.fleetops.defect.notice.FallbackDefectRecord
import com.acme.transport.fleetops.defect.notice.classifyRegionalDefectBundle

fun summarizeFallbackDefectRecordsForForecast(items: MutableList<FallbackDefectRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDefectForecastsFromRecord(items: MutableList<FallbackDefectRecord>): MutableList<DefectForecast> {
    val out = mutableListOf<DefectForecast>()
    for (item in items) {
        val derived = newDefectForecast(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateDefectForecast(item: DefectForecast): String = classifyRegionalDefectBundle(item.weightedLoad())
