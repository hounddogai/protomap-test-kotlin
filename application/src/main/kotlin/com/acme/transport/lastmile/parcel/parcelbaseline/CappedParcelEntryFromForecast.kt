package com.acme.transport.lastmile.parcel.parcelbaseline

import com.acme.transport.lastmile.drop.dropreservation.ScheduledDropForecast
import com.acme.transport.lastmile.drop.dropreservation.classifyScheduledDropForecast

fun summarizeScheduledDropForecastsForEntry(items: MutableList<ScheduledDropForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveCappedParcelEntriesFromForecast(items: MutableList<ScheduledDropForecast>): MutableList<CappedParcelEntry> {
    val out = mutableListOf<CappedParcelEntry>()
    for (item in items) {
        val derived = newCappedParcelEntry(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeCappedParcelEntry(item: CappedParcelEntry): String {
    return classifyScheduledDropForecast(item.weightedValue())
}
