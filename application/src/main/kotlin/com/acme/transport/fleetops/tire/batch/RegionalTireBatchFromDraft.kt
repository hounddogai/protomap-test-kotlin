package com.acme.transport.fleetops.tire.batch

import com.acme.transport.fleetops.leasereturn.draft.AdaptiveLeaseReturnDraft
import com.acme.transport.fleetops.leasereturn.draft.normalizeGlobalLeaseReturnProposalCode

fun summarizeAdaptiveLeaseReturnDraftsForBatch(items: MutableList<AdaptiveLeaseReturnDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRegionalTireBatchesFromDraft(items: MutableList<AdaptiveLeaseReturnDraft>): MutableList<RegionalTireBatch> {
    val out = mutableListOf<RegionalTireBatch>()
    for (item in items) {
        val derived = newRegionalTireBatch(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignRegionalTireBatch(item: RegionalTireBatch): String = normalizeGlobalLeaseReturnProposalCode(item.code)
