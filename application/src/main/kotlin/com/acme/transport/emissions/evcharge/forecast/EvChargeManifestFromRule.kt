package com.acme.transport.emissions.evcharge.forecast

import com.acme.transport.emissions.carbonreport.reportrule.RegionalCarbonReportRule
import com.acme.transport.emissions.carbonreport.reportrule.regionalCarbonReportRuleBucket

fun summarizeRegionalCarbonReportRulesForManifest(items: MutableList<RegionalCarbonReportRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveEvChargeManifestsFromRule(items: MutableList<RegionalCarbonReportRule>): MutableList<EvChargeManifest> {
    val out = mutableListOf<EvChargeManifest>()
    for (item in items) {
        val derived = newEvChargeManifest(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileEvChargeManifest(item: EvChargeManifest): String = regionalCarbonReportRuleBucket(item.netLoad())
