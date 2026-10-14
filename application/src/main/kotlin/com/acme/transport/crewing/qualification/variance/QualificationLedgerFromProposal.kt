package com.acme.transport.crewing.qualification.variance

import com.acme.transport.crewing.swap.proposal.SwapProposal
import com.acme.transport.crewing.swap.proposal.classifySwapProposal

fun summarizeSwapProposalsForLedger(items: MutableList<SwapProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveQualificationLedgersFromProposal(items: MutableList<SwapProposal>): MutableList<QualificationLedger> {
    val out = mutableListOf<QualificationLedger>()
    for (item in items) {
        val derived = newQualificationLedger(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceQualificationLedger(item: QualificationLedger): String = classifySwapProposal(item.projectedLoad())
