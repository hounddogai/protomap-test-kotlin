package com.acme.transport.emissions.carbonreport.claim

import com.acme.transport.emissions.carbonreport.digest.CarbonReportDigest
import com.acme.transport.emissions.carbonreport.digest.carbonReportDigestWeight

fun summarizeCarbonReportDigestsForClaim(items: MutableList<CarbonReportDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveCarbonReportClaimsFromDigest(items: MutableList<CarbonReportDigest>): MutableList<CarbonReportClaim> {
    val out = mutableListOf<CarbonReportClaim>()
    for (item in items) {
        val derived = newCarbonReportClaim(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeCarbonReportClaim(item: CarbonReportClaim): Double = carbonReportDigestWeight(7, true)
