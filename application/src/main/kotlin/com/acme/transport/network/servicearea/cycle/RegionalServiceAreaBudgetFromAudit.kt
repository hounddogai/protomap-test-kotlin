package com.acme.transport.network.servicearea.cycle

import com.acme.transport.network.cutoff.schedule.CutoffAudit
import com.acme.transport.network.cutoff.schedule.cutoffThresholdWeight

fun summarizeCutoffAuditsForBudget(items: MutableList<CutoffAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveRegionalServiceAreaBudgetsFromAudit(items: MutableList<CutoffAudit>): MutableList<RegionalServiceAreaBudget> {
    val out = mutableListOf<RegionalServiceAreaBudget>()
    for (item in items) {
        val derived = newRegionalServiceAreaBudget(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceRegionalServiceAreaBudget(item: RegionalServiceAreaBudget): Double = cutoffThresholdWeight(8, true)
