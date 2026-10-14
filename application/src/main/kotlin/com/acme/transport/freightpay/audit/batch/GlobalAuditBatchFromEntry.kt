package com.acme.transport.freightpay.audit.batch

import com.acme.transport.freightpay.ratecard.cardbundle.FallbackRateCardEntry
import com.acme.transport.freightpay.ratecard.cardbundle.globalRateCardBundleBucket

fun summarizeFallbackRateCardEntriesForBatch(items: MutableList<FallbackRateCardEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveGlobalAuditBatchesFromEntry(items: MutableList<FallbackRateCardEntry>): MutableList<GlobalAuditBatch> {
    val out = mutableListOf<GlobalAuditBatch>()
    for (item in items) {
        val derived = newGlobalAuditBatch(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectGlobalAuditBatch(item: GlobalAuditBatch): String = globalRateCardBundleBucket(item.measuredValue())
