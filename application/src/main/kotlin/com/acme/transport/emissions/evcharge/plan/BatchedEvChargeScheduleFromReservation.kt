package com.acme.transport.emissions.evcharge.plan

import com.acme.transport.emissions.routeoffset.audit.FallbackRouteOffsetReservation
import com.acme.transport.emissions.routeoffset.audit.normalizeRouteOffsetCycleCode

fun summarizeFallbackRouteOffsetReservationsForSchedule(items: MutableList<FallbackRouteOffsetReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveBatchedEvChargeSchedulesFromReservation(items: MutableList<FallbackRouteOffsetReservation>): MutableList<BatchedEvChargeSchedule> {
    val out = mutableListOf<BatchedEvChargeSchedule>()
    for (item in items) {
        val derived = newBatchedEvChargeSchedule(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun estimateBatchedEvChargeSchedule(item: BatchedEvChargeSchedule): String {
    return normalizeRouteOffsetCycleCode(item.code)
}
