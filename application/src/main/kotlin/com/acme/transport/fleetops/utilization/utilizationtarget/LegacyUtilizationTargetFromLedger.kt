package com.acme.transport.fleetops.utilization.utilizationtarget

import com.acme.transport.fleetops.serviceplan.quota.ServicePlanLedger
import com.acme.transport.fleetops.serviceplan.quota.classifyServicePlanLedger

fun summarizeServicePlanLedgersForTarget(items: MutableList<ServicePlanLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveLegacyUtilizationTargetsFromLedger(items: MutableList<ServicePlanLedger>): MutableList<LegacyUtilizationTarget> {
    val out = mutableListOf<LegacyUtilizationTarget>()
    for (item in items) {
        val derived = newLegacyUtilizationTarget(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectLegacyUtilizationTarget(item: LegacyUtilizationTarget): String {
    return classifyServicePlanLedger(item.adjustedVolume())
}
