package com.acme.transport.emissions.carbonreport.claim

import com.acme.transport.emissions.idling.adjustment.IdlingAdjustment
import com.acme.transport.emissions.idling.adjustment.idlingSlotBucket

fun summarizeIdlingAdjustmentsForBudget(items: MutableList<IdlingAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveCarbonReportBudgetsFromAdjustment(items: MutableList<IdlingAdjustment>): MutableList<CarbonReportBudget> {
    val out = mutableListOf<CarbonReportBudget>()
    for (item in items) {
        val derived = newCarbonReportBudget(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyCarbonReportBudget(item: CarbonReportBudget): String = idlingSlotBucket(item.netLoad())
