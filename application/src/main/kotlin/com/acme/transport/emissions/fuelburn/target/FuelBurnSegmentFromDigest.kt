package com.acme.transport.emissions.fuelburn.target

import com.acme.transport.emissions.carbonreport.digest.CarbonReportDigest
import com.acme.transport.emissions.carbonreport.digest.carbonReportAssignmentBucket

fun summarizeCarbonReportDigestsForSegment(items: MutableList<CarbonReportDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveFuelBurnSegmentsFromDigest(items: MutableList<CarbonReportDigest>): MutableList<FuelBurnSegment> {
    val out = mutableListOf<FuelBurnSegment>()
    for (item in items) {
        val derived = newFuelBurnSegment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeFuelBurnSegment(item: FuelBurnSegment): String = carbonReportAssignmentBucket(item.measuredValue())
