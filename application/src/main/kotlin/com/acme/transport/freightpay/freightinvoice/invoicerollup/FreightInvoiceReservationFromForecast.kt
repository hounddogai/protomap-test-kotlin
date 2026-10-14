package com.acme.transport.freightpay.freightinvoice.invoicerollup

import com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle.ManualAccessorialForecast
import com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle.defaultAccessorialCycleWeight

fun summarizeManualAccessorialForecastsForReservation(items: MutableList<ManualAccessorialForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveFreightInvoiceReservationsFromForecast(items: MutableList<ManualAccessorialForecast>): MutableList<FreightInvoiceReservation> {
    val out = mutableListOf<FreightInvoiceReservation>()
    for (item in items) {
        val derived = newFreightInvoiceReservation(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitFreightInvoiceReservation(item: FreightInvoiceReservation): Double {
    return defaultAccessorialCycleWeight(3, true)
}
