package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.freightpay.ratecard.cardsegment.WeightedRateCardQuota
import com.acme.transport.freightpay.ratecard.cardsegment.normalizeLegacyRateCardSegmentCode

fun summarizeWeightedRateCardQuotasForBatch(items: MutableList<WeightedRateCardQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun derivePrimaryAuditBatchesFromQuota(items: MutableList<WeightedRateCardQuota>): MutableList<PrimaryAuditBatch> {
    val out = mutableListOf<PrimaryAuditBatch>()
    for (item in items) {
        val derived = newPrimaryAuditBatch(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizePrimaryAuditBatch(item: PrimaryAuditBatch): String = normalizeLegacyRateCardSegmentCode(item.code)
