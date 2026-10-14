package com.acme.transport.freightpay.ratecard.variance

import com.acme.transport.freightpay.freightinvoice.invoicerollup.FreightInvoiceReservation

fun summarizeFreightInvoiceReservationsForThreshold(items: MutableList<FreightInvoiceReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveManualRateCardThresholdsFromReservation(items: MutableList<FreightInvoiceReservation>): MutableList<ManualRateCardThreshold> {
    val out = mutableListOf<ManualRateCardThreshold>()
    for (item in items) {
        val derived = newManualRateCardThreshold(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}
