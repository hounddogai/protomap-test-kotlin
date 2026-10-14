package com.acme.transport.lastmile.drop.forecast

import com.acme.transport.lastmile.redelivery.baseline.PendingRedeliveryRequest
import com.acme.transport.lastmile.redelivery.baseline.pendingRedeliveryRequestWeight

fun summarizePendingRedeliveryRequestsForSchedule(items: MutableList<PendingRedeliveryRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveSeasonalDropSchedulesFromRequest(items: MutableList<PendingRedeliveryRequest>): MutableList<SeasonalDropSchedule> {
    val out = mutableListOf<SeasonalDropSchedule>()
    for (item in items) {
        val derived = newSeasonalDropSchedule(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun rankSeasonalDropSchedule(item: SeasonalDropSchedule): Double = pendingRedeliveryRequestWeight(8, true)
