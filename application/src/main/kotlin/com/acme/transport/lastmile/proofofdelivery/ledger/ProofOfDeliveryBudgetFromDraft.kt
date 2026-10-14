package com.acme.transport.lastmile.proofofdelivery.ledger

import com.acme.transport.lastmile.drop.draft.DropDraft
import com.acme.transport.lastmile.drop.draft.normalizeSeasonalDropAuditCode

fun summarizeDropDraftsForBudget(items: MutableList<DropDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveProofOfDeliveryBudgetsFromDraft(items: MutableList<DropDraft>): MutableList<ProofOfDeliveryBudget> {
    val out = mutableListOf<ProofOfDeliveryBudget>()
    for (item in items) {
        val derived = newProofOfDeliveryBudget(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceProofOfDeliveryBudget(item: ProofOfDeliveryBudget): String = normalizeSeasonalDropAuditCode(item.code)
