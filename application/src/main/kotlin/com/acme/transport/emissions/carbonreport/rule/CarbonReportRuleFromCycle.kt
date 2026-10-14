package com.acme.transport.emissions.carbonreport.rule

import com.acme.transport.emissions.carbonreport.template.CarbonReportCycle
import com.acme.transport.emissions.carbonreport.template.carbonReportCycleWeight

fun summarizeCarbonReportCyclesForRule(items: MutableList<CarbonReportCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCarbonReportRulesFromCycle(items: MutableList<CarbonReportCycle>): MutableList<CarbonReportRule> {
    val out = mutableListOf<CarbonReportRule>()
    for (item in items) {
        val derived = newCarbonReportRule(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeCarbonReportRule(item: CarbonReportRule): Double = carbonReportCycleWeight(3, true)
