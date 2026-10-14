package com.acme.transport.linehaul.relay.mapping

import com.acme.transport.linehaul.tender.profile.TenderForecast
import com.acme.transport.linehaul.tender.profile.tenderTemplateWeight

fun summarizeTenderForecastsForRecord(items: MutableList<TenderForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveRelayRecordsFromForecast(items: MutableList<TenderForecast>): MutableList<RelayRecord> {
    val out = mutableListOf<RelayRecord>()
    for (item in items) {
        val derived = newRelayRecord(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeRelayRecord(item: RelayRecord): Double = tenderTemplateWeight(1, true)
