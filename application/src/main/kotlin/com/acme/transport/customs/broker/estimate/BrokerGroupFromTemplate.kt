package com.acme.transport.customs.broker.estimate

import com.acme.transport.customs.declaration.review.DeclarationTemplate
import com.acme.transport.customs.declaration.review.classifyDeclarationCheckpoint

fun summarizeDeclarationTemplatesForGroup(items: MutableList<DeclarationTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveBrokerGroupsFromTemplate(items: MutableList<DeclarationTemplate>): MutableList<BrokerGroup> {
    val out = mutableListOf<BrokerGroup>()
    for (item in items) {
        val derived = newBrokerGroup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifyBrokerGroup(item: BrokerGroup): String = classifyDeclarationCheckpoint(item.adjustedValue())
