package com.acme.transport.freightpay.audit.overrideitem

import com.acme.transport.freightpay.ratecard.draft.RateCardDraft
import com.acme.transport.freightpay.ratecard.draft.classifyRateCardDraft

fun summarizeRateCardDraftsForBaseline(items: MutableList<RateCardDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveAuditBaselinesFromDraft(items: MutableList<RateCardDraft>): MutableList<AuditBaseline> {
    val out = mutableListOf<AuditBaseline>()
    for (item in items) {
        val derived = newAuditBaseline(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignAuditBaseline(item: AuditBaseline): String = classifyRateCardDraft(item.measuredValue())
