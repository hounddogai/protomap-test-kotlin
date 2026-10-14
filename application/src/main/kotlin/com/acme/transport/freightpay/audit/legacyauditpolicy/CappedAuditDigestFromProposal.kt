package com.acme.transport.freightpay.audit.legacyauditpolicy

import com.acme.transport.freightpay.ratecard.score.RateCardProposal
import com.acme.transport.freightpay.ratecard.score.classifyDefaultRateCardPolicy

fun summarizeRateCardProposalsForDigest(items: MutableList<RateCardProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveCappedAuditDigestsFromProposal(items: MutableList<RateCardProposal>): MutableList<CappedAuditDigest> {
    val out = mutableListOf<CappedAuditDigest>()
    for (item in items) {
        val derived = newCappedAuditDigest(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceCappedAuditDigest(item: CappedAuditDigest): String {
    return classifyDefaultRateCardPolicy(item.adjustedExposure())
}
