package com.acme.transport.fleetops.defect.defectclaim

import com.acme.transport.fleetops.serviceplan.template.ManualServicePlanPreference
import com.acme.transport.fleetops.serviceplan.template.scheduledServicePlanTierWeight

fun summarizeManualServicePlanPreferencesForClaim(items: MutableList<ManualServicePlanPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveManualDefectClaimsFromPreference(items: MutableList<ManualServicePlanPreference>): MutableList<ManualDefectClaim> {
    val out = mutableListOf<ManualDefectClaim>()
    for (item in items) {
        val derived = newManualDefectClaim(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun deriveManualDefectClaim(item: ManualDefectClaim): Double = scheduledServicePlanTierWeight(2, true)
