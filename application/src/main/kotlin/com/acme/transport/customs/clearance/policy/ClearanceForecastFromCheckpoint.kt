package com.acme.transport.customs.clearance.policy

import com.acme.transport.customs.inspection.weightedinspectionplan.PendingInspectionCheckpoint
import com.acme.transport.customs.inspection.weightedinspectionplan.classifyPendingInspectionCheckpoint

fun summarizePendingInspectionCheckpointsForForecast(items: MutableList<PendingInspectionCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveClearanceForecastsFromCheckpoint(items: MutableList<PendingInspectionCheckpoint>): MutableList<ClearanceForecast> {
    val out = mutableListOf<ClearanceForecast>()
    for (item in items) {
        val derived = newClearanceForecast(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun scoreClearanceForecast(item: ClearanceForecast): String {
    return classifyPendingInspectionCheckpoint(item.adjustedValue())
}
