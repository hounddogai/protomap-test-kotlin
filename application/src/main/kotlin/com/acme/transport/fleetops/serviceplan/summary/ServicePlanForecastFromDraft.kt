package com.acme.transport.fleetops.serviceplan.summary

import com.acme.transport.fleetops.inspection.draft.InspectionDraft
import com.acme.transport.fleetops.inspection.draft.normalizeInspectionDraftCode

fun summarizeInspectionDraftsForForecast(items: MutableList<InspectionDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveServicePlanForecastsFromDraft(items: MutableList<InspectionDraft>): MutableList<ServicePlanForecast> {
    val out = mutableListOf<ServicePlanForecast>()
    for (item in items) {
        val derived = newServicePlanForecast(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planServicePlanForecast(item: ServicePlanForecast): String = normalizeInspectionDraftCode(item.code)
