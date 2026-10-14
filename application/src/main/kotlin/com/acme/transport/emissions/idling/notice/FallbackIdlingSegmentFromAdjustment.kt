package com.acme.transport.emissions.idling.notice

import com.acme.transport.emissions.idling.adjustment.IdlingAdjustment
import com.acme.transport.emissions.idling.adjustment.normalizeIdlingAdjustmentCode

fun summarizeIdlingAdjustmentsForSegment(items: MutableList<IdlingAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveFallbackIdlingSegmentsFromAdjustment(items: MutableList<IdlingAdjustment>): MutableList<FallbackIdlingSegment> {
    val out = mutableListOf<FallbackIdlingSegment>()
    for (item in items) {
        val derived = newFallbackIdlingSegment(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeFallbackIdlingSegment(item: FallbackIdlingSegment): String = normalizeIdlingAdjustmentCode(item.code)
