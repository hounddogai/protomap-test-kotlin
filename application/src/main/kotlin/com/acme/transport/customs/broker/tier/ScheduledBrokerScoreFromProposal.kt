package com.acme.transport.customs.broker.tier

import com.acme.transport.customs.declaration.budget.DeclarationProposal
import com.acme.transport.customs.declaration.budget.classifyRollingDeclarationTier

fun summarizeDeclarationProposalsForScore(items: MutableList<DeclarationProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveScheduledBrokerScoresFromProposal(items: MutableList<DeclarationProposal>): MutableList<ScheduledBrokerScore> {
    val out = mutableListOf<ScheduledBrokerScore>()
    for (item in items) {
        val derived = newScheduledBrokerScore(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeScheduledBrokerScore(item: ScheduledBrokerScore): String {
    return classifyRollingDeclarationTier(item.effectiveValue())
}
