package com.acme.transport.freightpay.freightinvoice.threshold

import com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment.PendingAccessorialForecast
import com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment.normalizePendingAccessorialForecastCode

fun summarizePendingAccessorialForecastsForThreshold(items: MutableList<PendingAccessorialForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRegionalFreightInvoiceThresholdsFromForecast(items: MutableList<PendingAccessorialForecast>): MutableList<RegionalFreightInvoiceThreshold> {
    val out = mutableListOf<RegionalFreightInvoiceThreshold>()
    for (item in items) {
        val derived = newRegionalFreightInvoiceThreshold(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreRegionalFreightInvoiceThreshold(item: RegionalFreightInvoiceThreshold): String {
    return normalizePendingAccessorialForecastCode(item.code)
}
