package com.acme.transport.customs.tariff.budget

import com.acme.transport.customs.broker.threshold.BatchedBrokerRule
import com.acme.transport.customs.broker.threshold.classifyScheduledBrokerThreshold

fun summarizeBatchedBrokerRulesForBudget(items: MutableList<BatchedBrokerRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveRegionalTariffBudgetsFromRule(items: MutableList<BatchedBrokerRule>): MutableList<RegionalTariffBudget> {
    val out = mutableListOf<RegionalTariffBudget>()
    for (item in items) {
        val derived = newRegionalTariffBudget(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeRegionalTariffBudget(item: RegionalTariffBudget): String {
    return classifyScheduledBrokerThreshold(item.projectedLoad())
}
