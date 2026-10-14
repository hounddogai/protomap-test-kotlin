package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment.PendingAccessorialForecast
import com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment.normalizePendingAccessorialForecastCode

fun summarizePendingAccessorialForecastsForEntry(items: MutableList<PendingAccessorialForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveGlobalRateCardEntriesFromForecast(items: MutableList<PendingAccessorialForecast>): MutableList<GlobalRateCardEntry> {
    val out = mutableListOf<GlobalRateCardEntry>()
    for (item in items) {
        val derived = newGlobalRateCardEntry(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planGlobalRateCardEntry(item: GlobalRateCardEntry): String {
    return normalizePendingAccessorialForecastCode(item.code)
}
