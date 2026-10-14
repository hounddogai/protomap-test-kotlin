package com.acme.transport.fleetops.utilization.proposal

import com.acme.transport.fleetops.serviceplan.profile.ServicePlanManifest
import com.acme.transport.fleetops.serviceplan.profile.servicePlanManifestWeight

fun summarizeServicePlanManifestsForPreference(items: MutableList<ServicePlanManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveUtilizationPreferencesFromManifest(items: MutableList<ServicePlanManifest>): MutableList<UtilizationPreference> {
    val out = mutableListOf<UtilizationPreference>()
    for (item in items) {
        val derived = newUtilizationPreference(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateUtilizationPreference(item: UtilizationPreference): Double = servicePlanManifestWeight(8, true)
