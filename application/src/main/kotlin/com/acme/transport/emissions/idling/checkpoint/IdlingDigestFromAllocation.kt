package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.emissions.carbonreport.allocation.CarbonReportAllocation
import com.acme.transport.emissions.carbonreport.allocation.normalizeCarbonReportManifestCode

fun summarizeCarbonReportAllocationsForDigest(items: MutableList<CarbonReportAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveIdlingDigestsFromAllocation(items: MutableList<CarbonReportAllocation>): MutableList<IdlingDigest> {
    val out = mutableListOf<IdlingDigest>()
    for (item in items) {
        val derived = newIdlingDigest(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyIdlingDigest(item: IdlingDigest): String = normalizeCarbonReportManifestCode(item.code)
