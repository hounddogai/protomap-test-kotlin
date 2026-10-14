package com.acme.transport.emissions.evcharge.milestone

import com.acme.transport.emissions.evcharge.slot.EvChargeForecast
import com.acme.transport.emissions.evcharge.slot.normalizeEvChargeForecastCode

fun summarizeEvChargeForecastsForTemplate(items: MutableList<EvChargeForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveEvChargeTemplatesFromForecast(items: MutableList<EvChargeForecast>): MutableList<EvChargeTemplate> {
    val out = mutableListOf<EvChargeTemplate>()
    for (item in items) {
        val derived = newEvChargeTemplate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun deriveEvChargeTemplate(item: EvChargeTemplate): String = normalizeEvChargeForecastCode(item.code)
