package com.acme.transport.safety.incident.batch

import com.acme.transport.safety.violation.variance.GlobalViolationBudget
import com.acme.transport.safety.violation.variance.globalViolationBudgetWeight

fun summarizeGlobalViolationBudgetsForBatch(items: MutableList<GlobalViolationBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveGlobalIncidentBatchesFromBudget(items: MutableList<GlobalViolationBudget>): MutableList<GlobalIncidentBatch> {
    val out = mutableListOf<GlobalIncidentBatch>()
    for (item in items) {
        val derived = newGlobalIncidentBatch(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignGlobalIncidentBatch(item: GlobalIncidentBatch): Double = globalViolationBudgetWeight(2, true)
