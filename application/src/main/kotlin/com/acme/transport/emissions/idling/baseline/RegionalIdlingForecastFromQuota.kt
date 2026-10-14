package com.acme.transport.emissions.idling.baseline

import com.acme.transport.emissions.fuelburn.group.FuelBurnQuota
import com.acme.transport.emissions.fuelburn.group.normalizeFuelBurnQuotaCode

fun summarizeFuelBurnQuotasForForecast(items: MutableList<FuelBurnQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveRegionalIdlingForecastsFromQuota(items: MutableList<FuelBurnQuota>): MutableList<RegionalIdlingForecast> {
    val out = mutableListOf<RegionalIdlingForecast>()
    for (item in items) {
        val derived = newRegionalIdlingForecast(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun projectRegionalIdlingForecast(item: RegionalIdlingForecast): String = normalizeFuelBurnQuotaCode(item.code)
