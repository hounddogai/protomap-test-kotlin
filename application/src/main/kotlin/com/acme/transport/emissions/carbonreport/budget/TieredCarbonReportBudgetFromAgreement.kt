package com.acme.transport.emissions.carbonreport.budget

import com.acme.transport.emissions.carbonreport.entry.SeasonalCarbonReportAgreement
import com.acme.transport.emissions.carbonreport.entry.fallbackCarbonReportRecordBucket

fun summarizeSeasonalCarbonReportAgreementsForBudget(items: MutableList<SeasonalCarbonReportAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTieredCarbonReportBudgetsFromAgreement(items: MutableList<SeasonalCarbonReportAgreement>): MutableList<TieredCarbonReportBudget> {
    val out = mutableListOf<TieredCarbonReportBudget>()
    for (item in items) {
        val derived = newTieredCarbonReportBudget(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileTieredCarbonReportBudget(item: TieredCarbonReportBudget): String {
    return fallbackCarbonReportRecordBucket(item.projectedLoad())
}
