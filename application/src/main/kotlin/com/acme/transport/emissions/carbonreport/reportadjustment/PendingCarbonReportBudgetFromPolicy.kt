package com.acme.transport.emissions.carbonreport.reportadjustment

import com.acme.transport.emissions.idling.window.AdaptiveIdlingPolicy
import com.acme.transport.emissions.idling.window.classifyIdlingWindow

fun summarizeAdaptiveIdlingPoliciesForBudget(items: MutableList<AdaptiveIdlingPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun derivePendingCarbonReportBudgetsFromPolicy(items: MutableList<AdaptiveIdlingPolicy>): MutableList<PendingCarbonReportBudget> {
    val out = mutableListOf<PendingCarbonReportBudget>()
    for (item in items) {
        val derived = newPendingCarbonReportBudget(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun classifyPendingCarbonReportBudgetExtra(item: PendingCarbonReportBudget): String {
    return classifyIdlingWindow(item.effectiveExposure())
}
