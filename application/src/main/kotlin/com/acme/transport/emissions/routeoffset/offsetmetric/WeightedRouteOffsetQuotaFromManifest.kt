package com.acme.transport.emissions.routeoffset.offsetmetric

import com.acme.transport.emissions.carbonreport.reportagreement.LenientCarbonReportManifest
import com.acme.transport.emissions.carbonreport.reportagreement.lenientCarbonReportManifestBucket

fun summarizeLenientCarbonReportManifestsForQuota(items: MutableList<LenientCarbonReportManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveWeightedRouteOffsetQuotasFromManifest(items: MutableList<LenientCarbonReportManifest>): MutableList<WeightedRouteOffsetQuota> {
    val out = mutableListOf<WeightedRouteOffsetQuota>()
    for (item in items) {
        val derived = newWeightedRouteOffsetQuota(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateWeightedRouteOffsetQuota(item: WeightedRouteOffsetQuota): String {
    return lenientCarbonReportManifestBucket(item.netExposure())
}
