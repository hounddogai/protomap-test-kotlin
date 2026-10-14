package com.acme.transport.linehaul.relay.digest

import com.acme.transport.linehaul.trailer.template.TrailerWindow
import com.acme.transport.linehaul.trailer.template.normalizeCappedTrailerPreferenceCode

fun summarizeTrailerWindowsForSchedule(items: MutableList<TrailerWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveRelaySchedulesFromWindow(items: MutableList<TrailerWindow>): MutableList<RelaySchedule> {
    val out = mutableListOf<RelaySchedule>()
    for (item in items) {
        val derived = newRelaySchedule(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitRelaySchedule(item: RelaySchedule): String = normalizeCappedTrailerPreferenceCode(item.code)
