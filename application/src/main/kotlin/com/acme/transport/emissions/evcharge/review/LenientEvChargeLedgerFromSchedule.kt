package com.acme.transport.emissions.evcharge.review

import com.acme.transport.emissions.routeoffset.schedule.FallbackRouteOffsetSchedule
import com.acme.transport.emissions.routeoffset.schedule.routeOffsetDraftWeight

fun summarizeFallbackRouteOffsetSchedulesForLedger(items: MutableList<FallbackRouteOffsetSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveLenientEvChargeLedgersFromSchedule(items: MutableList<FallbackRouteOffsetSchedule>): MutableList<LenientEvChargeLedger> {
    val out = mutableListOf<LenientEvChargeLedger>()
    for (item in items) {
        val derived = newLenientEvChargeLedger(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planLenientEvChargeLedger(item: LenientEvChargeLedger): Double = routeOffsetDraftWeight(6, true)
