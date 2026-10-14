package com.acme.transport.customs.tariff.cycle

import com.acme.transport.customs.clearance.policy.ClearanceForecast
import com.acme.transport.customs.clearance.policy.fallbackClearancePolicyWeight

fun summarizeClearanceForecastsForRequest(items: MutableList<ClearanceForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveDefaultTariffRequestsFromForecast(items: MutableList<ClearanceForecast>): MutableList<DefaultTariffRequest> {
    val out = mutableListOf<DefaultTariffRequest>()
    for (item in items) {
        val derived = newDefaultTariffRequest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun applyDefaultTariffRequest(item: DefaultTariffRequest): Double = fallbackClearancePolicyWeight(2, true)
