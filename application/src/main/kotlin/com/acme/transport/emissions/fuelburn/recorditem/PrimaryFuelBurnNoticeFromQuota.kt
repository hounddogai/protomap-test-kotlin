package com.acme.transport.emissions.fuelburn.recorditem

import com.acme.transport.emissions.routeoffset.notice.RouteOffsetQuota
import com.acme.transport.emissions.routeoffset.notice.routeOffsetNoticeBucket

fun summarizeRouteOffsetQuotasForNotice(items: MutableList<RouteOffsetQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun derivePrimaryFuelBurnNoticesFromQuota(items: MutableList<RouteOffsetQuota>): MutableList<PrimaryFuelBurnNotice> {
    val out = mutableListOf<PrimaryFuelBurnNotice>()
    for (item in items) {
        val derived = newPrimaryFuelBurnNotice(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyPrimaryFuelBurnNotice(item: PrimaryFuelBurnNotice): String {
    return routeOffsetNoticeBucket(item.weightedExposure())
}
