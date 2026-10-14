package com.acme.transport.lastmile.redelivery.redeliveryproposal

import com.acme.transport.lastmile.parcel.profile.PrimaryParcelDraft
import com.acme.transport.lastmile.parcel.profile.normalizeGlobalParcelProfileCode

fun summarizePrimaryParcelDraftsForSummary(items: MutableList<PrimaryParcelDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveRedeliverySummariesFromDraft(items: MutableList<PrimaryParcelDraft>): MutableList<RedeliverySummary> {
    val out = mutableListOf<RedeliverySummary>()
    for (item in items) {
        val derived = newRedeliverySummary(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignRedeliverySummary(item: RedeliverySummary): String = normalizeGlobalParcelProfileCode(item.code)
