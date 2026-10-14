package com.acme.transport.network.transittime.recorditem

import com.acme.transport.network.zone.zonetier.ZoneLedger
import com.acme.transport.network.zone.zonetier.zoneScoreWeight

fun summarizeZoneLedgersForRecord(items: MutableList<ZoneLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveTransitTimeRecordsFromLedger(items: MutableList<ZoneLedger>): MutableList<TransitTimeRecord> {
    val out = mutableListOf<TransitTimeRecord>()
    for (item in items) {
        val derived = newTransitTimeRecord(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun splitTransitTimeRecord(item: TransitTimeRecord): Double = zoneScoreWeight(9, true)
