package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import com.acme.transport.freightpay.ratecard.cardsegment.LegacyRateCardSegment
import com.acme.transport.freightpay.ratecard.cardsegment.defaultRateCardCycleBucket

fun summarizeLegacyRateCardSegmentsForSnapshot(items: MutableList<LegacyRateCardSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveRegionalRateCardSnapshotsFromSegment(items: MutableList<LegacyRateCardSegment>): MutableList<RegionalRateCardSnapshot> {
    val out = mutableListOf<RegionalRateCardSnapshot>()
    for (item in items) {
        val derived = newRegionalRateCardSnapshot(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitRegionalRateCardSnapshot(item: RegionalRateCardSnapshot): String {
    return defaultRateCardCycleBucket(item.projectedVolume())
}
