package com.acme.transport.network.transittime.digest

import com.acme.transport.network.zone.recorditem.ZoneRecord
import com.acme.transport.network.zone.recorditem.normalizeZoneRecordCode

fun summarizeZoneRecordsForVariance(items: MutableList<ZoneRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveTransitTimeVariancesFromRecord(items: MutableList<ZoneRecord>): MutableList<TransitTimeVariance> {
    val out = mutableListOf<TransitTimeVariance>()
    for (item in items) {
        val derived = newTransitTimeVariance(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeTransitTimeVariance(item: TransitTimeVariance): String = normalizeZoneRecordCode(item.code)
