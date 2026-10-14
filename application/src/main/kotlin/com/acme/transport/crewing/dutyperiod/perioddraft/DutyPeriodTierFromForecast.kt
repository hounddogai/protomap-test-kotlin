package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.crewing.availability.availabilityledger.GlobalAvailabilityForecast
import com.acme.transport.crewing.availability.availabilityledger.cappedAvailabilityPreferenceWeight

fun summarizeGlobalAvailabilityForecastsForTier(items: MutableList<GlobalAvailabilityForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveDutyPeriodTiersFromForecast(items: MutableList<GlobalAvailabilityForecast>): MutableList<DutyPeriodTier> {
    val out = mutableListOf<DutyPeriodTier>()
    for (item in items) {
        val derived = newDutyPeriodTier(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeDutyPeriodTier(item: DutyPeriodTier): Double = cappedAvailabilityPreferenceWeight(9, true)
