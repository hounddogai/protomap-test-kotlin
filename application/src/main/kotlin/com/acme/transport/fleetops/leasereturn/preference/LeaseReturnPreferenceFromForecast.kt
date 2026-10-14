package com.acme.transport.fleetops.leasereturn.preference

import com.acme.transport.fleetops.serviceplan.legacyplanrollup.ManualServicePlanForecast
import com.acme.transport.fleetops.serviceplan.legacyplanrollup.manualServicePlanForecastBucket

fun summarizeManualServicePlanForecastsForPreference(items: MutableList<ManualServicePlanForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLeaseReturnPreferencesFromForecast(items: MutableList<ManualServicePlanForecast>): MutableList<LeaseReturnPreference> {
    val out = mutableListOf<LeaseReturnPreference>()
    for (item in items) {
        val derived = newLeaseReturnPreference(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateLeaseReturnPreference(item: LeaseReturnPreference): String {
    return manualServicePlanForecastBucket(item.netValue())
}
