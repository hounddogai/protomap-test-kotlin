package com.acme.transport.emissions.emissionfactor.template

import com.acme.transport.emissions.carbonreport.reportestimate.CarbonReportMetric
import com.acme.transport.emissions.carbonreport.reportestimate.classifyCarbonReportMetric

fun summarizeCarbonReportMetricsForSlot(items: MutableList<CarbonReportMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveGlobalEmissionFactorSlotsFromMetric(items: MutableList<CarbonReportMetric>): MutableList<GlobalEmissionFactorSlot> {
    val out = mutableListOf<GlobalEmissionFactorSlot>()
    for (item in items) {
        val derived = newGlobalEmissionFactorSlot(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeGlobalEmissionFactorSlot(item: GlobalEmissionFactorSlot): String {
    return classifyCarbonReportMetric(item.effectiveValue())
}
