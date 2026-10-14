package com.acme.transport.fleetops.serviceplan.variance

import com.acme.transport.fleetops.serviceplan.planpreference.GlobalServicePlanPreference
import com.acme.transport.fleetops.serviceplan.planpreference.normalizeBatchedServicePlanSummaryCode

fun summarizeGlobalServicePlanPreferencesForLedger(items: MutableList<GlobalServicePlanPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePendingServicePlanLedgersFromPreference(items: MutableList<GlobalServicePlanPreference>): MutableList<PendingServicePlanLedger> {
    val out = mutableListOf<PendingServicePlanLedger>()
    for (item in items) {
        val derived = newPendingServicePlanLedger(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcilePendingServicePlanLedger(item: PendingServicePlanLedger): String {
    return normalizeBatchedServicePlanSummaryCode(item.code)
}
