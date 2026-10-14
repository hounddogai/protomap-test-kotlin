package com.acme.transport.emissions.carbonreport.variance

import com.acme.transport.emissions.evcharge.chargescore.AdaptiveEvChargeVariance
import com.acme.transport.emissions.evcharge.chargescore.batchedEvChargeScoreBucket

fun summarizeAdaptiveEvChargeVariancesForSegment(items: MutableList<AdaptiveEvChargeVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCarbonReportSegmentsFromVariance(items: MutableList<AdaptiveEvChargeVariance>): MutableList<CarbonReportSegment> {
    val out = mutableListOf<CarbonReportSegment>()
    for (item in items) {
        val derived = newCarbonReportSegment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeCarbonReportSegment(item: CarbonReportSegment): String = batchedEvChargeScoreBucket(item.measuredValue())
