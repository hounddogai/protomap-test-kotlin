package com.acme.transport.customs.inspection.inspectiontarget

import com.acme.transport.customs.declaration.window.PendingDeclarationForecast
import com.acme.transport.customs.declaration.window.classifyPendingDeclarationForecast

fun summarizePendingDeclarationForecastsForTarget(items: MutableList<PendingDeclarationForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveRegionalInspectionTargetsFromForecast(items: MutableList<PendingDeclarationForecast>): MutableList<RegionalInspectionTarget> {
    val out = mutableListOf<RegionalInspectionTarget>()
    for (item in items) {
        val derived = newRegionalInspectionTarget(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun mergeRegionalInspectionTarget(item: RegionalInspectionTarget): String {
    return classifyPendingDeclarationForecast(item.adjustedVolume())
}
