package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.emissions.carbonreport.rollup.CarbonReportRollup
import com.acme.transport.emissions.carbonreport.rollup.normalizeCarbonReportRollupCode

fun summarizeCarbonReportRollupsForBudget(items: MutableList<CarbonReportRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRegionalCarbonReportBudgetsFromRollup(items: MutableList<CarbonReportRollup>): MutableList<RegionalCarbonReportBudget> {
    val out = mutableListOf<RegionalCarbonReportBudget>()
    for (item in items) {
        val derived = newRegionalCarbonReportBudget(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceRegionalCarbonReportBudget(item: RegionalCarbonReportBudget): String {
    return normalizeCarbonReportRollupCode(item.code)
}
