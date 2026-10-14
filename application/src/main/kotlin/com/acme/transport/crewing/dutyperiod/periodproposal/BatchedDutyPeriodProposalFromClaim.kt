package com.acme.transport.crewing.dutyperiod.periodproposal

import com.acme.transport.crewing.swap.swapclaim.CappedSwapClaim
import com.acme.transport.crewing.swap.swapclaim.primarySwapMappingBucket

fun summarizeCappedSwapClaimsForProposal(items: MutableList<CappedSwapClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveBatchedDutyPeriodProposalsFromClaim(items: MutableList<CappedSwapClaim>): MutableList<BatchedDutyPeriodProposal> {
    val out = mutableListOf<BatchedDutyPeriodProposal>()
    for (item in items) {
        val derived = newBatchedDutyPeriodProposal(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun settleBatchedDutyPeriodProposal(item: BatchedDutyPeriodProposal): String {
    return primarySwapMappingBucket(item.netVolume())
}
