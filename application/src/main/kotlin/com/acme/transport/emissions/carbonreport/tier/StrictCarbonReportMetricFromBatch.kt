package com.acme.transport.emissions.carbonreport.tier

import com.acme.transport.emissions.carbonreport.adaptivereportrule.StrictCarbonReportBatch
import com.acme.transport.emissions.carbonreport.adaptivereportrule.classifyStrictCarbonReportBatch

fun summarizeStrictCarbonReportBatchesForMetric(items: MutableList<StrictCarbonReportBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveStrictCarbonReportMetricsFromBatch(items: MutableList<StrictCarbonReportBatch>): MutableList<StrictCarbonReportMetric> {
    val out = mutableListOf<StrictCarbonReportMetric>()
    for (item in items) {
        val derived = newStrictCarbonReportMetric(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scoreStrictCarbonReportMetric(item: StrictCarbonReportMetric): String {
    return classifyStrictCarbonReportBatch(item.netLoad())
}
