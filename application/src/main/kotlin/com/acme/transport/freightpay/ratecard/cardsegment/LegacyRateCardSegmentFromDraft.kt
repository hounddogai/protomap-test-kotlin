package com.acme.transport.freightpay.ratecard.cardsegment

import com.acme.transport.freightpay.fuelsurcharge.draft.ScheduledFuelSurchargeDraft
import com.acme.transport.freightpay.fuelsurcharge.draft.globalFuelSurchargeOverrideWeight

fun summarizeScheduledFuelSurchargeDraftsForSegment(items: MutableList<ScheduledFuelSurchargeDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveLegacyRateCardSegmentsFromDraft(items: MutableList<ScheduledFuelSurchargeDraft>): MutableList<LegacyRateCardSegment> {
    val out = mutableListOf<LegacyRateCardSegment>()
    for (item in items) {
        val derived = newLegacyRateCardSegment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankLegacyRateCardSegment(item: LegacyRateCardSegment): Double = globalFuelSurchargeOverrideWeight(5, true)
