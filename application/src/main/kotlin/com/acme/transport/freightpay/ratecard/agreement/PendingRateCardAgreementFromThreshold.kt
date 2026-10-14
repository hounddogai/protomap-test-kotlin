package com.acme.transport.freightpay.ratecard.agreement

import com.acme.transport.freightpay.ratecard.request.RateCardThreshold
import com.acme.transport.freightpay.ratecard.request.classifyRateCardThreshold

fun summarizeRateCardThresholdsForAgreement(items: MutableList<RateCardThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePendingRateCardAgreementsFromThreshold(items: MutableList<RateCardThreshold>): MutableList<PendingRateCardAgreement> {
    val out = mutableListOf<PendingRateCardAgreement>()
    for (item in items) {
        val derived = newPendingRateCardAgreement(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignPendingRateCardAgreement(item: PendingRateCardAgreement): String {
    return classifyRateCardThreshold(item.effectiveExposure())
}
