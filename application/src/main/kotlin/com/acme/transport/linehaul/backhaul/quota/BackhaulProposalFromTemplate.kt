package com.acme.transport.linehaul.backhaul.quota

import com.acme.transport.linehaul.relay.rule.RelayTemplate
import com.acme.transport.linehaul.relay.rule.classifyRelayTemplate

fun summarizeRelayTemplatesForProposal(items: MutableList<RelayTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveBackhaulProposalsFromTemplate(items: MutableList<RelayTemplate>): MutableList<BackhaulProposal> {
    val out = mutableListOf<BackhaulProposal>()
    for (item in items) {
        val derived = newBackhaulProposal(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateBackhaulProposal(item: BackhaulProposal): String = classifyRelayTemplate(item.projectedLoad())
