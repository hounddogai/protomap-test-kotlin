package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.freightpay.ratecard.rule.LenientRateCardEntry
import com.acme.transport.freightpay.ratecard.rule.adaptiveRateCardReservationWeight

fun summarizeLenientRateCardEntriesForTier(items: MutableList<LenientRateCardEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveLenientDisputeTiersFromEntry(items: MutableList<LenientRateCardEntry>): MutableList<LenientDisputeTier> {
    val out = mutableListOf<LenientDisputeTier>()
    for (item in items) {
        val derived = newLenientDisputeTier(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchLenientDisputeTier(item: LenientDisputeTier): Double = adaptiveRateCardReservationWeight(7, true)
