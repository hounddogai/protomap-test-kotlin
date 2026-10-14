package com.acme.transport.fleetops.serviceplan.rollup

import com.acme.transport.fleetops.tire.manifest.SeasonalTireRequest
import com.acme.transport.fleetops.tire.manifest.tireManifestWeight

fun summarizeSeasonalTireRequestsForDraft(items: MutableList<SeasonalTireRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveServicePlanDraftsFromRequest(items: MutableList<SeasonalTireRequest>): MutableList<ServicePlanDraft> {
    val out = mutableListOf<ServicePlanDraft>()
    for (item in items) {
        val derived = newServicePlanDraft(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun settleServicePlanDraft(item: ServicePlanDraft): Double = tireManifestWeight(6, true)
