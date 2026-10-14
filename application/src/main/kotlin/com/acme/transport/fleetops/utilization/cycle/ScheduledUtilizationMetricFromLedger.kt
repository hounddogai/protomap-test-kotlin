package com.acme.transport.fleetops.utilization.cycle

import com.acme.transport.fleetops.serviceplan.planledger.DefaultServicePlanLedger
import com.acme.transport.fleetops.serviceplan.planledger.normalizeDefaultServicePlanLedgerCode

fun summarizeDefaultServicePlanLedgersForMetric(items: MutableList<DefaultServicePlanLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveScheduledUtilizationMetricsFromLedger(items: MutableList<DefaultServicePlanLedger>): MutableList<ScheduledUtilizationMetric> {
    val out = mutableListOf<ScheduledUtilizationMetric>()
    for (item in items) {
        val derived = newScheduledUtilizationMetric(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignScheduledUtilizationMetric(item: ScheduledUtilizationMetric): String {
    return normalizeDefaultServicePlanLedgerCode(item.code)
}
