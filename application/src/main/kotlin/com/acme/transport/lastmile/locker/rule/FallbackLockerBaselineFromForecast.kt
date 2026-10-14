package com.acme.transport.lastmile.locker.rule

import com.acme.transport.lastmile.drop.dropreservation.ScheduledDropForecast
import com.acme.transport.lastmile.drop.dropreservation.strictDropAuditWeight

fun summarizeScheduledDropForecastsForBaseline(items: MutableList<ScheduledDropForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveFallbackLockerBaselinesFromForecast(items: MutableList<ScheduledDropForecast>): MutableList<FallbackLockerBaseline> {
    val out = mutableListOf<FallbackLockerBaseline>()
    for (item in items) {
        val derived = newFallbackLockerBaseline(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun splitFallbackLockerBaseline(item: FallbackLockerBaseline): Double = strictDropAuditWeight(1, true)
