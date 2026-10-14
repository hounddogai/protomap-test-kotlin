package com.acme.transport.freightpay.ratecard.cardgroup

import com.acme.transport.freightpay.audit.rollup.TieredAuditDraft
import com.acme.transport.freightpay.audit.rollup.normalizeTieredAuditDraftCode

fun summarizeTieredAuditDraftsForBaseline(items: MutableList<TieredAuditDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveLegacyRateCardBaselinesFromDraft(items: MutableList<TieredAuditDraft>): MutableList<LegacyRateCardBaseline> {
    val out = mutableListOf<LegacyRateCardBaseline>()
    for (item in items) {
        val derived = newLegacyRateCardBaseline(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignLegacyRateCardBaseline(item: LegacyRateCardBaseline): String = normalizeTieredAuditDraftCode(item.code)
