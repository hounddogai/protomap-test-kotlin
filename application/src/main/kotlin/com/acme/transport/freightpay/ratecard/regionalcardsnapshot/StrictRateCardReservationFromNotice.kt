package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import com.acme.transport.freightpay.freightinvoice.proposal.LegacyFreightInvoiceNotice
import com.acme.transport.freightpay.freightinvoice.proposal.strictFreightInvoiceRequestBucket

fun summarizeLegacyFreightInvoiceNoticesForReservation(items: MutableList<LegacyFreightInvoiceNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveStrictRateCardReservationsFromNotice(items: MutableList<LegacyFreightInvoiceNotice>): MutableList<StrictRateCardReservation> {
    val out = mutableListOf<StrictRateCardReservation>()
    for (item in items) {
        val derived = newStrictRateCardReservation(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyStrictRateCardReservation(item: StrictRateCardReservation): String {
    return strictFreightInvoiceRequestBucket(item.effectiveLoad())
}
