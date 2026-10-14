package com.acme.transport.freightpay.fuelsurcharge.segment

import com.acme.transport.freightpay.ratecard.reservation.LenientRateCardProfile
import com.acme.transport.freightpay.ratecard.reservation.lenientRateCardProfileWeight

fun summarizeLenientRateCardProfilesForForecast(items: MutableList<LenientRateCardProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveFallbackFuelSurchargeForecastsFromProfile(items: MutableList<LenientRateCardProfile>): MutableList<FallbackFuelSurchargeForecast> {
    val out = mutableListOf<FallbackFuelSurchargeForecast>()
    for (item in items) {
        val derived = newFallbackFuelSurchargeForecast(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeFallbackFuelSurchargeForecast(item: FallbackFuelSurchargeForecast): Double {
    return lenientRateCardProfileWeight(1, true)
}
