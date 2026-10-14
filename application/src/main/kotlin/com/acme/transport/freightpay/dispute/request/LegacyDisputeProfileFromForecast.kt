package com.acme.transport.freightpay.dispute.request

import com.acme.transport.freightpay.freightinvoice.budget.FreightInvoiceForecast
import com.acme.transport.freightpay.freightinvoice.budget.freightInvoiceForecastWeight

fun summarizeFreightInvoiceForecastsForProfile(items: MutableList<FreightInvoiceForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveLegacyDisputeProfilesFromForecast(items: MutableList<FreightInvoiceForecast>): MutableList<LegacyDisputeProfile> {
    val out = mutableListOf<LegacyDisputeProfile>()
    for (item in items) {
        val derived = newLegacyDisputeProfile(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun deriveLegacyDisputeProfile(item: LegacyDisputeProfile): Double = freightInvoiceForecastWeight(6, true)
