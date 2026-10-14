package com.acme.transport.freightpay.freightinvoice.invoicesummary

import com.acme.transport.freightpay.ratecard.policy.LenientRateCardPolicy
import com.acme.transport.freightpay.ratecard.policy.normalizeWeightedRateCardThresholdCode

fun summarizeLenientRateCardPoliciesForReservation(items: MutableList<LenientRateCardPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveAdaptiveFreightInvoiceReservationsFromPolicy(items: MutableList<LenientRateCardPolicy>): MutableList<AdaptiveFreightInvoiceReservation> {
    val out = mutableListOf<AdaptiveFreightInvoiceReservation>()
    for (item in items) {
        val derived = newAdaptiveFreightInvoiceReservation(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun resolveAdaptiveFreightInvoiceReservation(item: AdaptiveFreightInvoiceReservation): String {
    return normalizeWeightedRateCardThresholdCode(item.code)
}
