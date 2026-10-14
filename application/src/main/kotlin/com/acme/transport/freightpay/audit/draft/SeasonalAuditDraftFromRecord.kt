package com.acme.transport.freightpay.audit.draft

import com.acme.transport.freightpay.fuelsurcharge.template.FuelSurchargeRecord
import com.acme.transport.freightpay.fuelsurcharge.template.weightedFuelSurchargeProposalBucket

fun summarizeFuelSurchargeRecordsForDraft(items: MutableList<FuelSurchargeRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveSeasonalAuditDraftsFromRecord(items: MutableList<FuelSurchargeRecord>): MutableList<SeasonalAuditDraft> {
    val out = mutableListOf<SeasonalAuditDraft>()
    for (item in items) {
        val derived = newSeasonalAuditDraft(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateSeasonalAuditDraft(item: SeasonalAuditDraft): String {
    return weightedFuelSurchargeProposalBucket(item.effectiveValue())
}
