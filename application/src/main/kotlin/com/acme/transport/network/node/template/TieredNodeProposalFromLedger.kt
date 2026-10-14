package com.acme.transport.network.node.template

import com.acme.transport.network.hub.ledger.PendingHubLedger
import com.acme.transport.network.hub.ledger.adaptiveHubScoreWeight

fun summarizePendingHubLedgersForProposal(items: MutableList<PendingHubLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTieredNodeProposalsFromLedger(items: MutableList<PendingHubLedger>): MutableList<TieredNodeProposal> {
    val out = mutableListOf<TieredNodeProposal>()
    for (item in items) {
        val derived = newTieredNodeProposal(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeTieredNodeProposal(item: TieredNodeProposal): Double = adaptiveHubScoreWeight(1, true)
