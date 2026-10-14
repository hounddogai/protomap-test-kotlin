package com.acme.transport.linehaul.tender.template

import com.acme.transport.linehaul.capacity.segment.GlobalCapacityAgreement
import com.acme.transport.linehaul.capacity.segment.normalizeGlobalCapacityAgreementCode

fun summarizeGlobalCapacityAgreementsForProposal(items: MutableList<GlobalCapacityAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBatchedTenderProposalsFromAgreement(items: MutableList<GlobalCapacityAgreement>): MutableList<BatchedTenderProposal> {
    val out = mutableListOf<BatchedTenderProposal>()
    for (item in items) {
        val derived = newBatchedTenderProposal(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun splitBatchedTenderProposal(item: BatchedTenderProposal): String {
    return normalizeGlobalCapacityAgreementCode(item.code)
}
