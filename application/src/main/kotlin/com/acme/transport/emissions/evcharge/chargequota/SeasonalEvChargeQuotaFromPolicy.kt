package com.acme.transport.emissions.evcharge.chargequota

import com.acme.transport.emissions.routeoffset.estimate.RouteOffsetPolicy
import com.acme.transport.emissions.routeoffset.estimate.normalizeSeasonalRouteOffsetClaimCode

fun summarizeRouteOffsetPoliciesForQuota(items: MutableList<RouteOffsetPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveSeasonalEvChargeQuotasFromPolicy(items: MutableList<RouteOffsetPolicy>): MutableList<SeasonalEvChargeQuota> {
    val out = mutableListOf<SeasonalEvChargeQuota>()
    for (item in items) {
        val derived = newSeasonalEvChargeQuota(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileSeasonalEvChargeQuota(item: SeasonalEvChargeQuota): String {
    return normalizeSeasonalRouteOffsetClaimCode(item.code)
}
