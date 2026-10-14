package com.acme.transport.freightpay.accessorial.adjustment

import com.acme.transport.freightpay.ratecard.ledger.RateCardLedger
import com.acme.transport.freightpay.ratecard.ledger.classifyRateCardLedger

fun summarizeRateCardLedgersForTier(items: MutableList<RateCardLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveAccessorialTiersFromLedger(items: MutableList<RateCardLedger>): MutableList<AccessorialTier> {
    val out = mutableListOf<AccessorialTier>()
    for (item in items) {
        val derived = newAccessorialTier(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun deriveAccessorialTier(item: AccessorialTier): String = classifyRateCardLedger(item.weightedVolume())
