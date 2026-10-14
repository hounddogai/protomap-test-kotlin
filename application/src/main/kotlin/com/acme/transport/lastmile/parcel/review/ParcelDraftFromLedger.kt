package com.acme.transport.lastmile.parcel.review

import com.acme.transport.lastmile.redelivery.recorditem.RedeliveryLedger
import com.acme.transport.lastmile.redelivery.recorditem.normalizeRedeliveryLedgerCode

fun summarizeRedeliveryLedgersForDraft(items: MutableList<RedeliveryLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveParcelDraftsFromLedger(items: MutableList<RedeliveryLedger>): MutableList<ParcelDraft> {
    val out = mutableListOf<ParcelDraft>()
    for (item in items) {
        val derived = newParcelDraft(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateParcelDraft(item: ParcelDraft): String = normalizeRedeliveryLedgerCode(item.code)
