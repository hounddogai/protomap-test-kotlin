package com.acme.transport.customs.broker.budget

import com.acme.transport.customs.inspection.digest.InspectionSlot
import com.acme.transport.customs.inspection.digest.classifyInspectionDigest

fun summarizeInspectionSlotsForBudget(items: MutableList<InspectionSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveBrokerBudgetsFromSlot(items: MutableList<InspectionSlot>): MutableList<BrokerBudget> {
    val out = mutableListOf<BrokerBudget>()
    for (item in items) {
        val derived = newBrokerBudget(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun classifyBrokerBudget(item: BrokerBudget): String = classifyInspectionDigest(item.projectedTotal())
