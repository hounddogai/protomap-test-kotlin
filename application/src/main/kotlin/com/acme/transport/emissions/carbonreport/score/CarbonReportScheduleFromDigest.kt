package com.acme.transport.emissions.carbonreport.score

import com.acme.transport.emissions.idling.checkpoint.IdlingDigest
import com.acme.transport.emissions.idling.checkpoint.normalizeRegionalIdlingMappingCode

fun summarizeIdlingDigestsForSchedule(items: MutableList<IdlingDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveCarbonReportSchedulesFromDigest(items: MutableList<IdlingDigest>): MutableList<CarbonReportSchedule> {
    val out = mutableListOf<CarbonReportSchedule>()
    for (item in items) {
        val derived = newCarbonReportSchedule(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeCarbonReportSchedule(item: CarbonReportSchedule): String = normalizeRegionalIdlingMappingCode(item.code)
