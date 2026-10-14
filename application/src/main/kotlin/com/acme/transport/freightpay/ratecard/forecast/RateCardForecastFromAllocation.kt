package com.acme.transport.freightpay.ratecard.forecast

import com.acme.transport.freightpay.ratecard.cardsnapshot.RateCardAllocation
import com.acme.transport.freightpay.ratecard.cardsnapshot.normalizeRateCardAllocationCode

fun summarizeRateCardAllocationsForForecast(items: MutableList<RateCardAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveRateCardForecastsFromAllocation(items: MutableList<RateCardAllocation>): MutableList<RateCardForecast> {
    val out = mutableListOf<RateCardForecast>()
    for (item in items) {
        val derived = newRateCardForecast(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignRateCardForecast(item: RateCardForecast): String = normalizeRateCardAllocationCode(item.code)
