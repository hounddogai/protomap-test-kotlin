package com.acme.transport.emissions.routeoffset.batchedoffsetmetric

import com.acme.transport.emissions.fuelburn.burnmilestone.PendingFuelBurnTemplate
import com.acme.transport.emissions.fuelburn.burnmilestone.rollingFuelBurnMilestoneBucket

fun summarizePendingFuelBurnTemplatesForQuota(items: MutableList<PendingFuelBurnTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveGlobalRouteOffsetQuotasFromTemplate(items: MutableList<PendingFuelBurnTemplate>): MutableList<GlobalRouteOffsetQuota> {
    val out = mutableListOf<GlobalRouteOffsetQuota>()
    for (item in items) {
        val derived = newGlobalRouteOffsetQuota(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeGlobalRouteOffsetQuota(item: GlobalRouteOffsetQuota): String {
    return rollingFuelBurnMilestoneBucket(item.netTotal())
}
