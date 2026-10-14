package com.acme.transport.emissions.evcharge.chargepreference

import com.acme.transport.emissions.idling.idlingforecast.LegacyIdlingForecast
import com.acme.transport.emissions.idling.idlingforecast.pendingIdlingEntryWeight

fun summarizeLegacyIdlingForecastsForPreference(items: MutableList<LegacyIdlingForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveCappedEvChargePreferencesFromForecast(items: MutableList<LegacyIdlingForecast>): MutableList<CappedEvChargePreference> {
    val out = mutableListOf<CappedEvChargePreference>()
    for (item in items) {
        val derived = newCappedEvChargePreference(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun matchCappedEvChargePreference(item: CappedEvChargePreference): Double = pendingIdlingEntryWeight(5, true)
