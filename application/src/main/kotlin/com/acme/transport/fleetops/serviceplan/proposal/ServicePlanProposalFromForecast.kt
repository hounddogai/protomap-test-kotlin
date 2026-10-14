package com.acme.transport.fleetops.serviceplan.proposal

import com.acme.transport.fleetops.serviceplan.legacyplanrollup.ManualServicePlanForecast
import com.acme.transport.fleetops.serviceplan.legacyplanrollup.classifyManualServicePlanForecast

fun summarizeManualServicePlanForecastsForProposal(items: MutableList<ManualServicePlanForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveServicePlanProposalsFromForecast(items: MutableList<ManualServicePlanForecast>): MutableList<ServicePlanProposal> {
    val out = mutableListOf<ServicePlanProposal>()
    for (item in items) {
        val derived = newServicePlanProposal(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitServicePlanProposal(item: ServicePlanProposal): String {
    return classifyManualServicePlanForecast(item.projectedValue())
}
