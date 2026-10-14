package com.acme.transport.fleetops.utilization.audit

import com.acme.transport.fleetops.utilization.milestone.RollingUtilizationQuota
import com.acme.transport.fleetops.utilization.milestone.normalizeLegacyUtilizationSlotCode

fun summarizeRollingUtilizationQuotasForAudit(items: MutableList<RollingUtilizationQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveWeightedUtilizationAuditsFromQuota(items: MutableList<RollingUtilizationQuota>): MutableList<WeightedUtilizationAudit> {
    val out = mutableListOf<WeightedUtilizationAudit>()
    for (item in items) {
        val derived = newWeightedUtilizationAudit(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateWeightedUtilizationAudit(item: WeightedUtilizationAudit): String {
    return normalizeLegacyUtilizationSlotCode(item.code)
}
