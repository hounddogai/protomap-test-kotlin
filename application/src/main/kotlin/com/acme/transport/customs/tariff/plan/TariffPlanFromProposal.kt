package com.acme.transport.customs.tariff.plan

import com.acme.transport.customs.hold.proposal.HoldProposal
import com.acme.transport.customs.hold.proposal.pendingHoldProposalBucket

fun summarizeHoldProposalsForPlan(items: MutableList<HoldProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveTariffPlansFromProposal(items: MutableList<HoldProposal>): MutableList<TariffPlan> {
    val out = mutableListOf<TariffPlan>()
    for (item in items) {
        val derived = newTariffPlan(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchTariffPlan(item: TariffPlan): String = pendingHoldProposalBucket(item.adjustedLoad())
