package com.acme.transport.crewing.swap.swaprecord

import com.acme.transport.crewing.swap.swapmapping.FallbackSwapSegment
import com.acme.transport.crewing.swap.swapmapping.weightedSwapMappingWeight

fun summarizeFallbackSwapSegmentsForRecord(items: MutableList<FallbackSwapSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCappedSwapRecordsFromSegment(items: MutableList<FallbackSwapSegment>): MutableList<CappedSwapRecord> {
    val out = mutableListOf<CappedSwapRecord>()
    for (item in items) {
        val derived = newCappedSwapRecord(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun scoreCappedSwapRecord(item: CappedSwapRecord): Double = weightedSwapMappingWeight(9, true)
