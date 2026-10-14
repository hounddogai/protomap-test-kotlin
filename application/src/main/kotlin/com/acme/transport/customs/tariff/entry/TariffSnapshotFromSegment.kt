package com.acme.transport.customs.tariff.entry

import com.acme.transport.customs.hold.segment.WeightedHoldSegment
import com.acme.transport.customs.hold.segment.normalizeWeightedHoldSegmentCode

fun summarizeWeightedHoldSegmentsForSnapshot(items: MutableList<WeightedHoldSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveTariffSnapshotsFromSegment(items: MutableList<WeightedHoldSegment>): MutableList<TariffSnapshot> {
    val out = mutableListOf<TariffSnapshot>()
    for (item in items) {
        val derived = newTariffSnapshot(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun settleTariffSnapshot(item: TariffSnapshot): String = normalizeWeightedHoldSegmentCode(item.code)
