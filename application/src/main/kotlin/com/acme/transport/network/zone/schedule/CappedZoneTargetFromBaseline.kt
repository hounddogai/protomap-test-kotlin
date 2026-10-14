package com.acme.transport.network.zone.schedule

import com.acme.transport.network.transittime.preference.StrictTransitTimeBaseline
import com.acme.transport.network.transittime.preference.strictTransitTimeBaselineBucket

fun summarizeStrictTransitTimeBaselinesForTarget(items: MutableList<StrictTransitTimeBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveCappedZoneTargetsFromBaseline(items: MutableList<StrictTransitTimeBaseline>): MutableList<CappedZoneTarget> {
    val out = mutableListOf<CappedZoneTarget>()
    for (item in items) {
        val derived = newCappedZoneTarget(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchCappedZoneTarget(item: CappedZoneTarget): String = strictTransitTimeBaselineBucket(item.adjustedTotal())
