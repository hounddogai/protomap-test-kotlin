package com.acme.transport.network.servicearea.claim

import com.acme.transport.network.servicearea.areaassignment.ServiceAreaQuota
import com.acme.transport.network.servicearea.areaassignment.scheduledServiceAreaNoticeBucket

fun summarizeServiceAreaQuotasForClaim(items: MutableList<ServiceAreaQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveServiceAreaClaimsFromQuota(items: MutableList<ServiceAreaQuota>): MutableList<ServiceAreaClaim> {
    val out = mutableListOf<ServiceAreaClaim>()
    for (item in items) {
        val derived = newServiceAreaClaim(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeServiceAreaClaim(item: ServiceAreaClaim): String {
    return scheduledServiceAreaNoticeBucket(item.projectedExposure())
}
