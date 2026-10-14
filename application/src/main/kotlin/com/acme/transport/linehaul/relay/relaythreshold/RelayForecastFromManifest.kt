package com.acme.transport.linehaul.relay.relaythreshold

import com.acme.transport.linehaul.laneplan.manifest.LegacyLanePlanManifest
import com.acme.transport.linehaul.laneplan.manifest.normalizeScheduledLanePlanRecordCode

fun summarizeLegacyLanePlanManifestsForForecast(items: MutableList<LegacyLanePlanManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveRelayForecastsFromManifest(items: MutableList<LegacyLanePlanManifest>): MutableList<RelayForecast> {
    val out = mutableListOf<RelayForecast>()
    for (item in items) {
        val derived = newRelayForecast(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceRelayForecast(item: RelayForecast): String = normalizeScheduledLanePlanRecordCode(item.code)
