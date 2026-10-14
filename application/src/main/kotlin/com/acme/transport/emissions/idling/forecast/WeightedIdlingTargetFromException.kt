package com.acme.transport.emissions.idling.forecast

import com.acme.transport.emissions.carbonreport.reportrule.CarbonReportException
import com.acme.transport.emissions.carbonreport.reportrule.regionalCarbonReportRuleBucket

fun summarizeCarbonReportExceptionsForTarget(items: MutableList<CarbonReportException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveWeightedIdlingTargetsFromException(items: MutableList<CarbonReportException>): MutableList<WeightedIdlingTarget> {
    val out = mutableListOf<WeightedIdlingTarget>()
    for (item in items) {
        val derived = newWeightedIdlingTarget(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankWeightedIdlingTarget(item: WeightedIdlingTarget): String {
    return regionalCarbonReportRuleBucket(item.projectedValue())
}
