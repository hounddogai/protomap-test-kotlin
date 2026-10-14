package com.acme.transport.emissions.routeoffset.budget

import com.acme.transport.emissions.evcharge.notice.EvChargeBundle
import com.acme.transport.emissions.evcharge.notice.normalizeEvChargeBundleCode

fun summarizeEvChargeBundlesForVariance(items: MutableList<EvChargeBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveManualRouteOffsetVariancesFromBundle(items: MutableList<EvChargeBundle>): MutableList<ManualRouteOffsetVariance> {
    val out = mutableListOf<ManualRouteOffsetVariance>()
    for (item in items) {
        val derived = newManualRouteOffsetVariance(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignManualRouteOffsetVariance(item: ManualRouteOffsetVariance): String {
    return normalizeEvChargeBundleCode(item.code)
}
