package com.acme.transport.network.cutoff.baseline

import com.acme.transport.network.servicearea.primaryareatarget.ManualServiceAreaVariance
import com.acme.transport.network.servicearea.primaryareatarget.strictServiceAreaQuotaBucket

fun summarizeManualServiceAreaVariancesForReservation(items: MutableList<ManualServiceAreaVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveGlobalCutoffReservationsFromVariance(items: MutableList<ManualServiceAreaVariance>): MutableList<GlobalCutoffReservation> {
    val out = mutableListOf<GlobalCutoffReservation>()
    for (item in items) {
        val derived = newGlobalCutoffReservation(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun scoreGlobalCutoffReservation(item: GlobalCutoffReservation): String {
    return strictServiceAreaQuotaBucket(item.netExposure())
}
