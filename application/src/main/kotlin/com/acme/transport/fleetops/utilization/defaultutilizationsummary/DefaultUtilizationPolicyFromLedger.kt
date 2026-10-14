package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import com.acme.transport.fleetops.inspection.globalinspectionscore.LegacyInspectionLedger
import com.acme.transport.fleetops.inspection.globalinspectionscore.classifyInspectionTier

fun summarizeLegacyInspectionLedgersForPolicy(items: MutableList<LegacyInspectionLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveDefaultUtilizationPoliciesFromLedger(items: MutableList<LegacyInspectionLedger>): MutableList<DefaultUtilizationPolicy> {
    val out = mutableListOf<DefaultUtilizationPolicy>()
    for (item in items) {
        val derived = newDefaultUtilizationPolicy(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun projectDefaultUtilizationPolicy(item: DefaultUtilizationPolicy): String {
    return classifyInspectionTier(item.netTotal())
}
