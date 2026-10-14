package com.acme.transport.emissions.routeoffset.baseline

import com.acme.transport.emissions.carbonreport.digest.CarbonReportDigest
import com.acme.transport.emissions.carbonreport.digest.carbonReportDigestWeight

fun summarizeCarbonReportDigestsForBaseline(items: MutableList<CarbonReportDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveGlobalRouteOffsetBaselinesFromDigest(items: MutableList<CarbonReportDigest>): MutableList<GlobalRouteOffsetBaseline> {
    val out = mutableListOf<GlobalRouteOffsetBaseline>()
    for (item in items) {
        val derived = newGlobalRouteOffsetBaseline(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectGlobalRouteOffsetBaseline(item: GlobalRouteOffsetBaseline): Double = carbonReportDigestWeight(7, true)
