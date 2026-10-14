package com.acme.transport.emissions.carbonreport.reportestimate

import com.acme.transport.emissions.idling.regionalidlingmanifest.FallbackIdlingManifest
import com.acme.transport.emissions.idling.regionalidlingmanifest.fallbackIdlingManifestBucket

fun summarizeFallbackIdlingManifestsForMetric(items: MutableList<FallbackIdlingManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveCarbonReportMetricsFromManifest(items: MutableList<FallbackIdlingManifest>): MutableList<CarbonReportMetric> {
    val out = mutableListOf<CarbonReportMetric>()
    for (item in items) {
        val derived = newCarbonReportMetric(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeCarbonReportMetric(item: CarbonReportMetric): String {
    return fallbackIdlingManifestBucket(item.projectedTotal())
}
