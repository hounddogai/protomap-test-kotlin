package com.acme.transport.fleetops.serviceplan.planprofile

import com.acme.transport.fleetops.utilization.budget.CappedUtilizationPreference
import com.acme.transport.fleetops.utilization.budget.classifyUtilizationAgreement

fun summarizeCappedUtilizationPreferencesForMetric(items: MutableList<CappedUtilizationPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveLegacyServicePlanMetricsFromPreference(items: MutableList<CappedUtilizationPreference>): MutableList<LegacyServicePlanMetric> {
    val out = mutableListOf<LegacyServicePlanMetric>()
    for (item in items) {
        val derived = newLegacyServicePlanMetric(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeLegacyServicePlanMetric(item: LegacyServicePlanMetric): String {
    return classifyUtilizationAgreement(item.weightedExposure())
}
