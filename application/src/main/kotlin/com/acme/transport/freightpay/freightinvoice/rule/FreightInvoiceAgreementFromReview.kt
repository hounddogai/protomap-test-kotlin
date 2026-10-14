package com.acme.transport.freightpay.freightinvoice.rule

import com.acme.transport.freightpay.accessorial.cycle.AccessorialReview
import com.acme.transport.freightpay.accessorial.cycle.accessorialReviewWeight

fun summarizeAccessorialReviewsForAgreement(items: MutableList<AccessorialReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveFreightInvoiceAgreementsFromReview(items: MutableList<AccessorialReview>): MutableList<FreightInvoiceAgreement> {
    val out = mutableListOf<FreightInvoiceAgreement>()
    for (item in items) {
        val derived = newFreightInvoiceAgreement(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun scoreFreightInvoiceAgreement(item: FreightInvoiceAgreement): Double = accessorialReviewWeight(2, true)
