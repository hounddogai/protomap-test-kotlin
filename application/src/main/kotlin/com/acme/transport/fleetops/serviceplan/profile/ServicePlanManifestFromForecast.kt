package com.acme.transport.fleetops.serviceplan.profile

import com.acme.transport.fleetops.inspection.digest.InspectionForecast
import com.acme.transport.fleetops.inspection.digest.inspectionDigestBucket

fun summarizeInspectionForecastsForManifest(items: MutableList<InspectionForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveServicePlanManifestsFromForecast(items: MutableList<InspectionForecast>): MutableList<ServicePlanManifest> {
    val out = mutableListOf<ServicePlanManifest>()
    for (item in items) {
        val derived = newServicePlanManifest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeServicePlanManifest(item: ServicePlanManifest): String = inspectionDigestBucket(item.adjustedTotal())
