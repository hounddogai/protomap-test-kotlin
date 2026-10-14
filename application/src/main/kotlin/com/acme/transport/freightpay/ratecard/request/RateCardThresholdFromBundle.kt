package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.freightpay.dispute.bundle.ManualDisputeBundle
import com.acme.transport.freightpay.dispute.bundle.normalizeDisputeReservationCode

fun summarizeManualDisputeBundlesForThreshold(items: MutableList<ManualDisputeBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRateCardThresholdsFromBundle(items: MutableList<ManualDisputeBundle>): MutableList<RateCardThreshold> {
    val out = mutableListOf<RateCardThreshold>()
    for (item in items) {
        val derived = newRateCardThreshold(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeRateCardThreshold(item: RateCardThreshold): String = normalizeDisputeReservationCode(item.code)
