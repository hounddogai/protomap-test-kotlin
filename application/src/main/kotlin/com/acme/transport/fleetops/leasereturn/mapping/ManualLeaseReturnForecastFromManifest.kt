package com.acme.transport.fleetops.leasereturn.mapping

import com.acme.transport.fleetops.serviceplan.assignment.WeightedServicePlanManifest
import com.acme.transport.fleetops.serviceplan.assignment.classifyManualServicePlanAssignment

fun summarizeWeightedServicePlanManifestsForForecast(items: MutableList<WeightedServicePlanManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveManualLeaseReturnForecastsFromManifest(items: MutableList<WeightedServicePlanManifest>): MutableList<ManualLeaseReturnForecast> {
    val out = mutableListOf<ManualLeaseReturnForecast>()
    for (item in items) {
        val derived = newManualLeaseReturnForecast(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeManualLeaseReturnForecast(item: ManualLeaseReturnForecast): String {
    return classifyManualServicePlanAssignment(item.projectedVolume())
}
