package com.acme.transport.emissions.carbonreport.tier

import com.acme.transport.emissions.routeoffset.offsetnotice.DefaultRouteOffsetBaseline
import com.acme.transport.emissions.routeoffset.offsetnotice.defaultRouteOffsetBaselineBucket

fun summarizeDefaultRouteOffsetBaselinesForMetric(items: MutableList<DefaultRouteOffsetBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveStrictCarbonReportMetricsFromBaseline(items: MutableList<DefaultRouteOffsetBaseline>): MutableList<StrictCarbonReportMetric> {
    val out = mutableListOf<StrictCarbonReportMetric>()
    for (item in items) {
        val derived = newStrictCarbonReportMetric(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyStrictCarbonReportMetric(item: StrictCarbonReportMetric): String {
    return defaultRouteOffsetBaselineBucket(item.netLoad())
}
