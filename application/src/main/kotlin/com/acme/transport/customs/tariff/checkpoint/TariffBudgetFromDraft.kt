package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.customs.broker.estimate.BrokerDraft
import com.acme.transport.customs.broker.estimate.brokerEstimateBucket

fun summarizeBrokerDraftsForBudget(items: MutableList<BrokerDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveTariffBudgetsFromDraft(items: MutableList<BrokerDraft>): MutableList<TariffBudget> {
    val out = mutableListOf<TariffBudget>()
    for (item in items) {
        val derived = newTariffBudget(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileTariffBudget(item: TariffBudget): String = brokerEstimateBucket(item.adjustedTotal())
