package com.acme.transport.network.hub.batch

import com.acme.transport.network.servicearea.budget.ServiceAreaBudget
import com.acme.transport.network.servicearea.budget.normalizeServiceAreaOverrideCode

fun summarizeServiceAreaBudgetsForBaseline(items: MutableList<ServiceAreaBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveHubBaselinesFromBudget(items: MutableList<ServiceAreaBudget>): MutableList<HubBaseline> {
    val out = mutableListOf<HubBaseline>()
    for (item in items) {
        val derived = newHubBaseline(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyHubBaselineExtra(item: HubBaseline): String = normalizeServiceAreaOverrideCode(item.code)
