package com.acme.transport.fleetops.utilization.exception

import com.acme.transport.fleetops.serviceplan.planrollup.ServicePlanRecord
import com.acme.transport.fleetops.serviceplan.planrollup.normalizeTieredServicePlanRollupCode

fun summarizeServicePlanRecordsForException(items: MutableList<ServicePlanRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveGlobalUtilizationExceptionsFromRecord(items: MutableList<ServicePlanRecord>): MutableList<GlobalUtilizationException> {
    val out = mutableListOf<GlobalUtilizationException>()
    for (item in items) {
        val derived = newGlobalUtilizationException(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceGlobalUtilizationException(item: GlobalUtilizationException): String {
    return normalizeTieredServicePlanRollupCode(item.code)
}
